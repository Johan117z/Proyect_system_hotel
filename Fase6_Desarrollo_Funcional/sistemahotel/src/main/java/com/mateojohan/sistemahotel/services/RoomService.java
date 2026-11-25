package com.mateojohan.sistemahotel.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mateojohan.sistemahotel.dto.RoomRateDTO;
import com.mateojohan.sistemahotel.dto.RoomRequestDTO;
import com.mateojohan.sistemahotel.dto.RoomResponseDTO;
import com.mateojohan.sistemahotel.dto.RoomStatusDTO;
import com.mateojohan.sistemahotel.dto.RoomTypeChangeDTO;
import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.enums.RoomStatus;
import com.mateojohan.sistemahotel.enums.RoomType;
import com.mateojohan.sistemahotel.exceptions.BusinessRuleException;
import com.mateojohan.sistemahotel.exceptions.InvalidDataException;
import com.mateojohan.sistemahotel.exceptions.ResourceNotFoundException;
import com.mateojohan.sistemahotel.models.Room;
import com.mateojohan.sistemahotel.models.RoomFactory;
import com.mateojohan.sistemahotel.repositories.ReservationRepository;
import com.mateojohan.sistemahotel.repositories.RoomRepository;

/** Épica 1: Habitaciones (HU-01 a HU-04) y disponibilidad (HU-11). */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /** HU-01 / RN-01: número de habitación único. */
    @Transactional
    public RoomResponseDTO create(RoomRequestDTO request) {
        if (roomRepository.existsByRoomNumber(request.roomNumber())) {
            throw new BusinessRuleException("RN-01", "El número de habitación ya se encuentra registrado.");
        }
        BigDecimal fee = validateFee(request.type(), request.extraServiceFee());
        Room room = RoomFactory.create(request.type(), request.roomNumber(), request.capacity(),
                request.pricePerNight(), fee);
        return RoomResponseDTO.from(roomRepository.save(room));
    }

    /** HU-04: todas las habitaciones activas, o solo las de un estado. */
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> findAll(RoomStatus status) {
        List<Room> rooms = status == null
                ? roomRepository.findByActiveTrueOrderByRoomNumberAsc()
                : roomRepository.findByActiveTrueAndStatusOrderByRoomNumberAsc(status);
        return rooms.stream().map(RoomResponseDTO::from).toList();
    }

    /** HU-02: si el número no existe, informa que la habitación no fue encontrada. */
    @Transactional(readOnly = true)
    public RoomResponseDTO findByNumber(Integer roomNumber) {
        return RoomResponseDTO.from(getActiveRoom(roomNumber));
    }

    /** HU-11 / RN-07: habitaciones libres en un rango de fechas. */
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        validateRange(checkIn, checkOut);
        return roomRepository.findAvailable(checkIn, checkOut, RoomStatus.MAINTENANCE, ReservationStatus.BLOCKING)
                .stream().map(RoomResponseDTO::from).toList();
    }

    /** HU-02: cambio de tarifa. Las facturas emitidas conservan la tarifa copiada (RN-12). */
    @Transactional
    public RoomResponseDTO updateRate(Integer roomNumber, RoomRateDTO request) {
        Room room = getActiveRoom(roomNumber);
        room.setPricePerNight(request.pricePerNight());
        return RoomResponseDTO.from(room);
    }

    /** HU-02 / RN-14: cambio de tipo solo si la habitación no tiene reservas pendientes o activas. */
    @Transactional
    public RoomResponseDTO changeType(Integer roomNumber, RoomTypeChangeDTO request) {
        Room room = getActiveRoom(roomNumber);
        if (reservationRepository.existsByRoomAndStatusIn(room, ReservationStatus.BLOCKING)) {
            throw new BusinessRuleException("RN-14",
                    "No se puede cambiar el tipo: la habitación " + roomNumber + " tiene reservas pendientes o activas.");
        }
        BigDecimal fee = validateFee(request.type(), request.extraServiceFee());
        roomRepository.changeType(room.getId(), request.type().name(), request.capacity(),
                request.pricePerNight(), fee);
        return RoomResponseDTO.from(getActiveRoom(roomNumber));   // se relee: ahora es otra subclase
    }

    /** RN-15: manual solo AVAILABLE <-> MAINTENANCE. OCCUPIED lo asignan el check-in y el check-out. */
    @Transactional
    public RoomResponseDTO changeStatus(Integer roomNumber, RoomStatusDTO request) {
        Room room = getActiveRoom(roomNumber);
        if (request.status() == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("RN-15", "El estado OCCUPIED solo se asigna al hacer check-in.");
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("RN-15", "La habitación está ocupada; se libera con el check-out.");
        }
        room.changeStatus(request.status());
        return RoomResponseDTO.from(room);
    }

    /** HU-03 / RN-02: baja lógica, solo si no está ocupada ni tiene reservas futuras. */
    @Transactional
    public void delete(Integer roomNumber) {
        Room room = getActiveRoom(roomNumber);
        if (room.getStatus() == RoomStatus.OCCUPIED
                || reservationRepository.existsByRoomAndStatusIn(room, ReservationStatus.BLOCKING)) {
            throw new BusinessRuleException("RN-02",
                    "No se puede eliminar: la habitación " + roomNumber + " está ocupada o tiene reservas futuras asociadas.");
        }
        room.deactivate();
    }

    /** Uso interno de otros servicios (ReservationService). */
    Room getActiveRoom(Integer roomNumber) {
        return roomRepository.findByRoomNumberAndActiveTrue(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("La habitación " + roomNumber + " no fue encontrada."));
    }

    /** CHECK ck_rooms_fee_suite: solo la suite lleva cargo extra. */
    private static BigDecimal validateFee(RoomType type, BigDecimal fee) {
        BigDecimal value = fee == null ? BigDecimal.ZERO : fee;
        if (type != RoomType.SUITE && value.signum() != 0) {
            throw new InvalidDataException("extraServiceFee", "Solo las habitaciones SUITE tienen cargo extra.");
        }
        return value;
    }

    static void validateRange(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDataException("checkOutDate", "La fecha de salida debe ser posterior a la de entrada.");
        }
    }
}
