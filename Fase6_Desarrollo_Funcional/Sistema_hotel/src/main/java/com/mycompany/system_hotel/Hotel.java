package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de aplicación: orquesta las entidades y aplica las reglas de negocio.
 * En la migración a Spring Boot sus métodos pasan casi uno a uno a RoomService,
 * GuestService y ReservationService.
 */
public class Hotel {

    private final String name;
    private final List<IRoom> rooms = new ArrayList<>();
    private final List<IRoom> retiredRooms = new ArrayList<>();   // FR-04: baja lógica (rooms.is_active = 0)
    private final List<Guest> guests = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
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

    // ===================== Módulo 1: Habitaciones =====================

    /** FR-01: el número es único, incluso frente a habitaciones dadas de baja (rooms.room_number UNIQUE). */
    public void registerRoom(IRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("La habitación es obligatoria.");
        }
        boolean duplicated = rooms.stream().anyMatch(r -> r.getNumber() == room.getNumber())
                || retiredRooms.stream().anyMatch(r -> r.getNumber() == room.getNumber());
        if (duplicated) {
            throw new BusinessRuleException("FR-01", "Ya existe una habitación con el número " + room.getNumber() + ".");
        }
        rooms.add(room);
    }

    public IRoom findRoom(int number) {
        return rooms.stream()
                .filter(r -> r.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No existe la habitación " + number + "."));
    }

    /** FR-02 */
    public List<IRoom> getAllRooms() {
        return List.copyOf(rooms);
    }

    /** FR-02 */
    public List<IRoom> getRoomsByStatus(RoomStatus status) {
        return rooms.stream().filter(r -> r.getStatus() == status).toList();
    }

    /** FR-03: cambio de tarifa. No afecta facturas emitidas (BR-03) porque Invoice copia la tarifa. */
    public void updateRoomRate(int number, BigDecimal newPrice) {
        findRoom(number).setPricePerNight(newPrice);
    }

    /**
     * FR-03: cambio de tipo. Como el tipo define la subclase, se reemplaza el objeto.
     * Solo se permite si la habitación no tiene reservas pendientes o activas (BR-05).
     */
    public IRoom changeRoomType(int number, RoomType newType, int capacity,
                                BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        IRoom current = findRoom(number);
        if (hasBlockingReservations(current)) {
            throw new BusinessRuleException("BR-05",
                    "No se puede cambiar el tipo: la habitación " + number + " tiene reservas pendientes o activas.");
        }
        Room replacement = RoomFactory.create(newType, number, capacity, pricePerNight, extraServiceFee);
        replacement.changeStatus(current.getStatus());
        rooms.set(rooms.indexOf(current), replacement);
        return replacement;
    }

    /**
     * Cambio manual de estado (mantenimiento). OCCUPIED solo se asigna con check-in (FR-08)
     * y una habitación ocupada solo se libera con check-out (FR-09).
     */
    public void changeRoomStatus(int number, RoomStatus newStatus) {
        IRoom room = findRoom(number);
        if (newStatus == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("BR-06", "El estado OCCUPIED solo se asigna al hacer check-in.");
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("BR-06", "La habitación está ocupada; se libera con el check-out.");
        }
        room.changeStatus(newStatus);
    }

    /** FR-04: baja lógica, solo si no hay reservas pendientes o activas. Conserva el historial. */
    public void deleteRoom(int number) {
        IRoom room = findRoom(number);
        if (hasBlockingReservations(room)) {
            throw new BusinessRuleException("FR-04",
                    "No se puede eliminar: la habitación " + number + " tiene reservas pendientes o activas.");
        }
        rooms.remove(room);
        retiredRooms.add(room);
    }

    private boolean hasBlockingReservations(IRoom room) {
        return reservations.stream()
                .anyMatch(r -> r.getRoom() == room && r.getStatus().blocksRoom());
    }

    // ===================== Módulo 2: Huéspedes =====================

    /** FR-05 */
    public void registerGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("El huésped es obligatorio.");
        }
        if (guests.stream().anyMatch(g -> g.getDocument().equals(guest.getDocument()))) {
            throw new BusinessRuleException("FR-05", "Ya existe un huésped con el documento " + guest.getDocument() + ".");
        }
        guests.add(guest);
    }

    /** FR-06 */
    public Guest findGuest(String document) {
        String doc = document == null ? "" : document.trim();
        return guests.stream()
                .filter(g -> g.getDocument().equals(doc))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No existe un huésped con el documento " + doc + "."));
    }

    /** FR-06 */
    public void updateGuestContact(String document, String email, String phone) {
        findGuest(document).updateContactInfo(email, phone);
    }

    public List<Guest> getAllGuests() {
        return List.copyOf(guests);
    }

    // ===================== Módulo 3: Reservas y operación =====================

    /** FR-07 con BR-01 (sin sobreventa) y BR-04 (no reservar en mantenimiento ni en el pasado). */
    public Reservation createReservation(String guestDocument, int roomNumber,
                                         LocalDate checkInDate, LocalDate checkOutDate) {
        Guest guest = findGuest(guestDocument);
        IRoom room = findRoom(roomNumber);

        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias.");
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("BR-04", "La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!room.isBookable()) {
            throw new BusinessRuleException("BR-04", "La habitación " + roomNumber + " está en mantenimiento.");
        }
        boolean overlap = reservations.stream()
                .anyMatch(r -> r.getRoom() == room && r.overlaps(checkInDate, checkOutDate));
        if (overlap) {
            throw new BusinessRuleException("BR-01",
                    "La habitación " + roomNumber + " ya está reservada en ese rango de fechas.");
        }

        Reservation reservation = new Reservation(nextReservationCode(checkInDate), guest, room,
                checkInDate, checkOutDate);
        reservations.add(reservation);
        return reservation;
    }

    /** Código RES-AAAA-NNN, igual al formato de reservations.reservation_code. */
    private String nextReservationCode(LocalDate checkInDate) {
        return String.format("RES-%d-%03d", checkInDate.getYear(), reservationSequence++);
    }

    public Reservation findReservation(String reservationCode) {
        String code = reservationCode == null ? "" : reservationCode.trim().toUpperCase();
        return reservations.stream()
                .filter(r -> r.getReservationCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No existe la reserva " + code + "."));
    }

    /** FR-08 */
    public Reservation checkIn(String reservationCode) {
        Reservation reservation = findReservation(reservationCode);
        reservation.checkIn();
        return reservation;
    }

    /** FR-09: cierra la reserva y emite la factura en una sola operación. */
    public Invoice checkOut(String reservationCode, BigDecimal additionalServices) {
        Reservation reservation = findReservation(reservationCode);
        reservation.checkOut();
        Invoice invoice = new Invoice(invoiceSequence++, reservation, additionalServices);
        invoices.add(invoice);
        return invoice;
    }

    /** FR-10 */
    public Reservation cancelReservation(String reservationCode) {
        Reservation reservation = findReservation(reservationCode);
        reservation.cancel();
        return reservation;
    }

    public List<Reservation> getAllReservations() {
        return List.copyOf(reservations);
    }

    public List<Invoice> getAllInvoices() {
        return List.copyOf(invoices);
    }
}
