package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Servicio de aplicación: orquesta las entidades y aplica las reglas de negocio de las 14 historias
 * de usuario (HU-01 a HU-14). En la migración a Spring Boot sus métodos pasan casi uno a uno a
 * RoomService, GuestService, ReservationService y BillingService.
 */
public class Hotel {

    private final String name;
    private final List<IRoom> rooms = new ArrayList<>();
    private final List<IRoom> retiredRooms = new ArrayList<>();     // HU-03: baja lógica (rooms.is_active = 0)
    private final List<Guest> guests = new ArrayList<>();
    private final List<Guest> retiredGuests = new ArrayList<>();    // HU-07: baja lógica (guests.is_active = 0)
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private int reservationSequence = 1;
    private int invoiceSequence = 1;

    public Hotel(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del hotel es obligatorio.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    // ===================== Épica 1: Habitaciones (HU-01 a HU-04) =====================

    /** HU-01 / RN-01: el número es único, incluso frente a habitaciones dadas de baja (rooms.room_number UNIQUE). */
    public void registerRoom(IRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("La habitación es obligatoria.");
        }
        boolean duplicated = rooms.stream().anyMatch(r -> r.getNumber() == room.getNumber())
                || retiredRooms.stream().anyMatch(r -> r.getNumber() == room.getNumber());
        if (duplicated) {
            throw new BusinessRuleException("RN-01", "El número de habitación ya se encuentra registrado.");
        }
        rooms.add(room);
    }

    /** HU-02: si el número no existe, informa que la habitación no fue encontrada. */
    public IRoom findRoom(int number) {
        return rooms.stream()
                .filter(r -> r.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("La habitación " + number + " no fue encontrada."));
    }

    /** HU-04 */
    public List<IRoom> getAllRooms() {
        return List.copyOf(rooms);
    }

    /** HU-04 / HU-11 */
    public List<IRoom> getRoomsByStatus(RoomStatus status) {
        return rooms.stream().filter(r -> r.getStatus() == status).toList();
    }

    /** HU-02: cambio de tarifa. No afecta facturas emitidas (RN-12) porque Invoice copia la tarifa. */
    public void updateRoomRate(int number, BigDecimal newPrice) {
        findRoom(number).setPricePerNight(newPrice);
    }

    /**
     * HU-02: cambio de tipo y capacidad. Como el tipo define la subclase, se reemplaza el objeto.
     * Solo se permite si la habitación no tiene reservas pendientes o activas (RN-14).
     */
    public IRoom changeRoomType(int number, RoomType newType, int capacity,
                                BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        IRoom current = findRoom(number);
        if (hasBlockingReservations(current)) {
            throw new BusinessRuleException("RN-14",
                    "No se puede cambiar el tipo: la habitación " + number + " tiene reservas pendientes o activas.");
        }
        Room replacement = RoomFactory.create(newType, number, capacity, pricePerNight, extraServiceFee);
        replacement.changeStatus(current.getStatus());
        rooms.set(rooms.indexOf(current), replacement);
        return replacement;
    }

    /**
     * Cambio manual de estado: AVAILABLE ↔ MAINTENANCE (por ejemplo, habilitar una habitación después
     * de la limpieza). OCCUPIED solo se asigna con el check-in y se libera con el check-out (RN-15).
     */
    public void changeRoomStatus(int number, RoomStatus newStatus) {
        IRoom room = findRoom(number);
        if (newStatus == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("RN-15", "El estado OCCUPIED solo se asigna al hacer check-in.");
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("RN-15", "La habitación está ocupada; se libera con el check-out.");
        }
        room.changeStatus(newStatus);
    }

    /** HU-03 / RN-02: baja lógica, solo si no está ocupada ni tiene reservas futuras. Conserva el historial. */
    public void deleteRoom(int number) {
        IRoom room = findRoom(number);
        if (hasBlockingReservations(room)) {
            throw new BusinessRuleException("RN-02",
                    "No se puede eliminar: la habitación " + number + " está ocupada o tiene reservas futuras asociadas.");
        }
        rooms.remove(room);
        retiredRooms.add(room);
    }

    private boolean hasBlockingReservations(IRoom room) {
        return reservations.stream()
                .anyMatch(r -> r.getRoom() == room && r.getStatus().blocksRoom());
    }

    // ===================== Épica 2: Huéspedes (HU-05 a HU-08) =====================

    /** HU-05 / RN-03: el documento es único, incluso frente a huéspedes dados de baja (guests.document UNIQUE). */
    public void registerGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("El huésped es obligatorio.");
        }
        boolean duplicated = guests.stream().anyMatch(g -> g.getDocument().equals(guest.getDocument()))
                || retiredGuests.stream().anyMatch(g -> g.getDocument().equals(guest.getDocument()));
        if (duplicated) {
            throw new BusinessRuleException("RN-03", "El huésped ya existe en el sistema.");
        }
        guests.add(guest);
    }

    /** HU-06: si el documento no existe, informa que el huésped no fue encontrado. */
    public Guest findGuest(String document) {
        String doc = document == null ? "" : document.trim();
        return guests.stream()
                .filter(g -> g.getDocument().equals(doc))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("El huésped con documento " + doc + " no fue encontrado."));
    }

    /** HU-06: solo datos de contacto. */
    public void updateGuestContact(String document, String email, String phone) {
        findGuest(document).updateContactInfo(email, phone);
    }

    /** HU-06: datos personales y de contacto. */
    public void updateGuest(String document, String fullName, String email, String phone) {
        findGuest(document).updatePersonalInfo(fullName, email, phone);
    }

    /**
     * HU-07 / RN-04: baja lógica, solo si el huésped no tiene hospedaje activo, reservas vigentes
     * ni facturas sin pagar. Conserva su historial de reservas y facturas.
     */
    public void deleteGuest(String document) {
        Guest guest = findGuest(document);
        boolean hasLiveReservations = reservations.stream()
                .anyMatch(r -> r.getGuest() == guest && r.getStatus().blocksRoom());
        boolean hasUnpaidInvoices = invoices.stream()
                .anyMatch(i -> i.getReservation().getGuest() == guest && !isPaid(i));
        if (hasLiveReservations || hasUnpaidInvoices) {
            throw new BusinessRuleException("RN-04",
                    "No se puede eliminar: el huésped tiene hospedaje activo, reservas vigentes o facturas sin pagar.");
        }
        guests.remove(guest);
        retiredGuests.add(guest);
    }

    /** HU-08: busca por nombre, apellido o documento (sin distinguir mayúsculas ni tildes). */
    public List<Guest> searchGuests(String text) {
        String query = normalize(text);
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Ingrese un nombre, apellido o documento para buscar.");
        }
        return guests.stream()
                .filter(g -> normalize(g.getFullName()).contains(query) || g.getDocument().contains(query))
                .toList();
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    public List<Guest> getAllGuests() {
        return List.copyOf(guests);
    }

    // ===================== Épica 3: Reservas (HU-09 a HU-11) =====================

    /** HU-09 / RN-05 (sin cruce de fechas) y RN-13 (no reservar en mantenimiento ni en el pasado). */
    public Reservation createReservation(String guestDocument, int roomNumber,
                                         LocalDate checkInDate, LocalDate checkOutDate) {
        Guest guest = findGuest(guestDocument);
        IRoom room = findRoom(roomNumber);

        validateRange(checkInDate, checkOutDate);
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("RN-13", "La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!room.isBookable()) {
            throw new BusinessRuleException("RN-13", "La habitación " + roomNumber + " está en mantenimiento.");
        }
        if (isBookedBetween(room, checkInDate, checkOutDate)) {
            throw new BusinessRuleException("RN-05", "La habitación no está disponible en el rango de fechas seleccionado.");
        }

        Reservation reservation = new Reservation(nextReservationCode(checkInDate), guest, room,
                checkInDate, checkOutDate);
        reservations.add(reservation);
        reservationSequence++;   // solo se consume el número si la reserva se creó
        return reservation;
    }

    /**
     * HU-11 / RN-07: habitaciones sin cruce de fechas con reservas pendientes o activas en el rango.
     * Se excluyen las que están en mantenimiento, porque no se pueden reservar (RN-13).
     */
    public List<IRoom> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        validateRange(checkInDate, checkOutDate);
        return rooms.stream()
                .filter(IRoom::isBookable)
                .filter(r -> !isBookedBetween(r, checkInDate, checkOutDate))
                .toList();
    }

    private boolean isBookedBetween(IRoom room, LocalDate checkInDate, LocalDate checkOutDate) {
        return reservations.stream()
                .anyMatch(r -> r.getRoom() == room && r.overlaps(checkInDate, checkOutDate));
    }

    private static void validateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada.");
        }
    }

    /** Código RES-AAAA-NNN, igual al formato de reservations.reservation_code. */
    private String nextReservationCode(LocalDate checkInDate) {
        return String.format("RES-%d-%03d", checkInDate.getYear(), reservationSequence);
    }

    public Reservation findReservation(String reservationCode) {
        String code = reservationCode == null ? "" : reservationCode.trim().toUpperCase();
        return reservations.stream()
                .filter(r -> r.getReservationCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No existe la reserva " + code + "."));
    }

    /** HU-10 / RN-06: cancela una reserva pendiente y libera sus fechas. */
    public Reservation cancelReservation(String reservationCode) {
        Reservation reservation = findReservation(reservationCode);
        reservation.cancel();
        return reservation;
    }

    public List<Reservation> getAllReservations() {
        return List.copyOf(reservations);
    }

    // ===================== Épica 4: Estadía (HU-12, HU-13) =====================

    /** HU-12 / RN-08: la reserva pasa a ACTIVE y la habitación a OCCUPIED. */
    public Reservation checkIn(String reservationCode) {
        Reservation reservation = findReservation(reservationCode);
        reservation.checkIn(LocalDate.now());
        return reservation;
    }

    /** Registra un servicio adicional consumido durante la estadía. No se admiten después de facturar. */
    public AdditionalService addService(String reservationCode, String description, BigDecimal amount) {
        Reservation reservation = findReservation(reservationCode);
        if (findInvoiceFor(reservation).isPresent()) {
            throw new BusinessRuleException("RN-12",
                    "La estadía ya fue facturada; no se pueden agregar más servicios.");
        }
        AdditionalService service = new AdditionalService(description, amount);
        reservation.addService(service);
        return service;
    }

    /**
     * HU-13 / RN-09 y RN-10: solo con la cuenta saldada (factura emitida y pagada).
     * La reserva pasa a COMPLETED y la habitación a MAINTENANCE (Mantenimiento/Limpieza).
     */
    public Reservation checkOut(String reservationCode) {
        Reservation reservation = findReservation(reservationCode);
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("RF-13",
                    "Solo se puede hacer check-out de reservas activas. Estado actual: " + reservation.getStatus() + ".");
        }
        Optional<Invoice> invoice = findInvoiceFor(reservation);
        if (invoice.isEmpty()) {
            throw new BusinessRuleException("RN-09",
                    "La cuenta tiene saldo pendiente: genere la factura y registre el pago antes del check-out.");
        }
        if (!isPaid(invoice.get())) {
            throw new BusinessRuleException("RN-09",
                    "La cuenta tiene un saldo pendiente de " + ConsoleUtils.formatCop(invoice.get().getTotalAmount())
                            + ". Registre el pago antes del check-out.");
        }
        reservation.checkOut();
        return reservation;
    }

    // ===================== Épica 5: Facturación (HU-14) =====================

    /** HU-14 / RN-11: factura de una estadía en curso. Una reserva tiene como máximo una factura. */
    public Invoice generateInvoice(String reservationCode) {
        Reservation reservation = findReservation(reservationCode);
        if (findInvoiceFor(reservation).isPresent()) {
            throw new BusinessRuleException("RN-12", "La estadía " + reservation.getReservationCode() + " ya fue facturada.");
        }
        Invoice invoice = new Invoice(invoiceSequence, reservation);
        invoices.add(invoice);
        invoiceSequence++;
        return invoice;
    }

    /** Registra el pago total de una factura; con esto la cuenta queda saldada (RN-09). */
    public Payment registerPayment(int invoiceId, PaymentMethod method) {
        Invoice invoice = findInvoice(invoiceId);
        if (isPaid(invoice)) {
            throw new BusinessRuleException("RN-09", "La factura No. " + invoiceId + " ya está pagada.");
        }
        Payment payment = new Payment(invoice, method);
        payments.add(payment);
        return payment;
    }

    public Invoice findInvoice(int invoiceId) {
        return invoices.stream()
                .filter(i -> i.getInvoiceId() == invoiceId)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No existe la factura No. " + invoiceId + "."));
    }

    public Optional<Invoice> findInvoiceFor(Reservation reservation) {
        return invoices.stream().filter(i -> i.getReservation() == reservation).findFirst();
    }

    public boolean isPaid(Invoice invoice) {
        return payments.stream().anyMatch(p -> p.getInvoice() == invoice);
    }

    public List<Invoice> getAllInvoices() {
        return List.copyOf(invoices);
    }

    public List<Payment> getAllPayments() {
        return List.copyOf(payments);
    }
}
