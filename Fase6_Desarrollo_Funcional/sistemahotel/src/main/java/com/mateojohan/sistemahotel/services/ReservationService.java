package com.mateojohan.sistemahotel.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mateojohan.sistemahotel.dto.AdditionalServiceRequestDTO;
import com.mateojohan.sistemahotel.dto.ReservationRequestDTO;
import com.mateojohan.sistemahotel.dto.ReservationResponseDTO;
import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.exceptions.BusinessRuleException;
import com.mateojohan.sistemahotel.exceptions.ResourceNotFoundException;
import com.mateojohan.sistemahotel.models.AdditionalService;
import com.mateojohan.sistemahotel.models.Guest;
import com.mateojohan.sistemahotel.models.Invoice;
import com.mateojohan.sistemahotel.models.Reservation;
import com.mateojohan.sistemahotel.models.Room;
import com.mateojohan.sistemahotel.repositories.AdditionalServiceRepository;
import com.mateojohan.sistemahotel.repositories.InvoiceRepository;
import com.mateojohan.sistemahotel.repositories.PaymentRepository;
import com.mateojohan.sistemahotel.repositories.ReservationRepository;
import com.mateojohan.sistemahotel.utils.FormatUtils;

/** Épica 3: Reservas (HU-09 a HU-11) y Épica 4: Estadía (HU-12, HU-13). */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final GuestService guestService;
    private final RoomService roomService;
    private final ReservationCodeGenerator codeGenerator;

    public ReservationService(ReservationRepository reservationRepository,
                              AdditionalServiceRepository additionalServiceRepository,
                              InvoiceRepository invoiceRepository,
                              PaymentRepository paymentRepository,
                              GuestService guestService,
                              RoomService roomService,
                              ReservationCodeGenerator codeGenerator) {
        this.reservationRepository = reservationRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.guestService = guestService;
        this.roomService = roomService;
        this.codeGenerator = codeGenerator;
    }

    /** HU-09 / RN-05 (sin cruce de fechas) y RN-13 (ni en mantenimiento ni en el pasado). */
    @Transactional
    public ReservationResponseDTO create(ReservationRequestDTO request) {
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        // Validaciones que no consultan la base de datos
        RoomService.validateRange(checkIn, checkOut);
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("RN-13", "La fecha de entrada no puede ser anterior a hoy.");
        }

        // Primera consulta de la transacción: toma el consecutivo con bloqueo (ver ReservationCodeGenerator).
        // Desde aquí las reservas se crean de una en una, así el control de cruce (RN-05) es confiable.
        String code = codeGenerator.next(checkIn);

        Guest guest = guestService.getActiveGuest(request.guestDocument());
        Room room = roomService.getActiveRoom(request.roomNumber());
        if (!room.isBookable()) {
            throw new BusinessRuleException("RN-13", "La habitación " + room.getRoomNumber() + " está en mantenimiento.");
        }
        if (reservationRepository.existsOverlap(room, checkIn, checkOut, ReservationStatus.BLOCKING)) {
            throw new BusinessRuleException("RN-05", "La habitación no está disponible en el rango de fechas seleccionado.");
        }

        Reservation reservation = new Reservation(code, guest, room, checkIn, checkOut);
        return ReservationResponseDTO.from(reservationRepository.save(reservation));
    }

    /** Lista todas las reservas o solo las de un estado. */
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findAll(ReservationStatus status) {
        List<Reservation> reservations = status == null
                ? reservationRepository.findAllByOrderByIdDesc()
                : reservationRepository.findByStatusOrderByCheckInDateAsc(status);
        return reservations.stream().map(ReservationResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO findByCode(String code) {
        return ReservationResponseDTO.from(getReservation(code));
    }

    /** HU-10 / RN-06: cancela una reserva pendiente y libera sus fechas. */
    @Transactional
    public ReservationResponseDTO cancel(String code) {
        Reservation reservation = getReservation(code);
        reservation.cancel();
        return ReservationResponseDTO.from(reservation);
    }

    /** HU-12 / RN-08: la reserva pasa a ACTIVE y la habitación a OCCUPIED. */
    @Transactional
    public ReservationResponseDTO checkIn(String code) {
        Reservation reservation = getReservation(code);
        reservation.checkIn(LocalDate.now());
        return ReservationResponseDTO.from(reservation);
    }

    /** Servicio adicional durante la estadía. No se admite si la estadía ya fue facturada (RN-12). */
    @Transactional
    public ReservationResponseDTO addService(String code, AdditionalServiceRequestDTO request) {
        Reservation reservation = getReservation(code);
        if (invoiceRepository.existsByReservation(reservation)) {
            throw new BusinessRuleException("RN-12", "La estadía ya fue facturada; no se pueden agregar más servicios.");
        }
        AdditionalService service = new AdditionalService(reservation, request.description(), request.amount());
        reservation.addService(service);   // valida que la reserva esté ACTIVE (RF-14)
        additionalServiceRepository.save(service);
        return ReservationResponseDTO.from(reservation);
    }

    /**
     * HU-13 / RN-09 y RN-10: solo con la cuenta saldada (factura emitida y pagada).
     * La reserva pasa a COMPLETED y la habitación a MAINTENANCE.
     */
    @Transactional
    public ReservationResponseDTO checkOut(String code) {
        Reservation reservation = getReservation(code);
        reservation.ensureActive("RF-13", "Solo se puede hacer check-out de reservas activas.");
        Invoice invoice = invoiceRepository.findByReservation(reservation)
                .orElseThrow(() -> new BusinessRuleException("RN-09",
                        "La cuenta tiene saldo pendiente: genere la factura y registre el pago antes del check-out."));
        if (!paymentRepository.existsByInvoice(invoice)) {
            throw new BusinessRuleException("RN-09", "La cuenta tiene un saldo pendiente de "
                    + FormatUtils.formatCop(invoice.getTotalAmount()) + ". Registre el pago antes del check-out.");
        }
        reservation.checkOut();
        return ReservationResponseDTO.from(reservation);
    }

    /** Uso interno de BillingService. */
    Reservation getReservation(String code) {
        String clean = code == null ? "" : code.trim().toUpperCase();
        return reservationRepository.findByReservationCode(clean)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la reserva " + clean + "."));
    }
}
