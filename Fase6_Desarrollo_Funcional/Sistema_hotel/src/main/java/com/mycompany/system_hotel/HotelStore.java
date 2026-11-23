package com.mycompany.system_hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Almacenamiento en memoria de los datos del hotel (RS-02). Su única responsabilidad es guardar,
 * buscar y numerar registros; no aplica reglas de negocio (eso lo hacen los servicios).
 * Cumple el papel de los repositorios: al conectar la base de datos de la Fase 4, esta clase
 * se reemplaza por repositorios sobre las tablas rooms, guests, reservations, invoices y payments.
 */
public class HotelStore {

    private final List<IRoom> rooms = new ArrayList<>();
    private final List<IRoom> retiredRooms = new ArrayList<>();     // rooms.is_active = 0
    private final List<Guest> guests = new ArrayList<>();
    private final List<Guest> retiredGuests = new ArrayList<>();    // guests.is_active = 0
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private int reservationSequence = 1;
    private int invoiceSequence = 1;

    // ===================== Habitaciones =====================

    public List<IRoom> getActiveRooms() {
        return List.copyOf(rooms);
    }

    /** Incluye las habitaciones dadas de baja, porque su número sigue siendo único (rooms.room_number UNIQUE). */
    public boolean roomNumberExists(int number) {
        return rooms.stream().anyMatch(r -> r.getNumber() == number)
                || retiredRooms.stream().anyMatch(r -> r.getNumber() == number);
    }

    public IRoom findRoom(int number) {
        return rooms.stream()
                .filter(r -> r.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("La habitación " + number + " no fue encontrada."));
    }

    public void addRoom(IRoom room) {
        rooms.add(room);
    }

    public void replaceRoom(IRoom current, IRoom replacement) {
        rooms.set(rooms.indexOf(current), replacement);
    }

    public void retireRoom(IRoom room) {
        rooms.remove(room);
        retiredRooms.add(room);
    }

    // ===================== Huéspedes =====================

    public List<Guest> getActiveGuests() {
        return List.copyOf(guests);
    }

    /** Incluye los huéspedes dados de baja, porque su documento sigue siendo único (guests.document UNIQUE). */
    public boolean documentExists(String document) {
        return guests.stream().anyMatch(g -> g.getDocument().equals(document))
                || retiredGuests.stream().anyMatch(g -> g.getDocument().equals(document));
    }

    public Guest findGuest(String document) {
        String doc = document == null ? "" : document.trim();
        return guests.stream()
                .filter(g -> g.getDocument().equals(doc))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("El huésped con documento " + doc + " no fue encontrado."));
    }

    public void addGuest(Guest guest) {
        guests.add(guest);
    }

    public void retireGuest(Guest guest) {
        guests.remove(guest);
        retiredGuests.add(guest);
    }

    // ===================== Reservas =====================

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public Reservation findReservation(String reservationCode) {
        String code = reservationCode == null ? "" : reservationCode.trim().toUpperCase();
        return reservations.stream()
                .filter(r -> r.getReservationCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No existe la reserva " + code + "."));
    }

    /** Siguiente número del consecutivo RES-AAAA-NNN; solo avanza al guardar la reserva. */
    public int nextReservationNumber() {
        return reservationSequence;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservationSequence++;
    }

    // ===================== Facturas y pagos =====================

    public List<Invoice> getInvoices() {
        return List.copyOf(invoices);
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

    /** Siguiente número de factura; solo avanza al guardar la factura. */
    public int nextInvoiceNumber() {
        return invoiceSequence;
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        invoiceSequence++;
    }

    public List<Payment> getPayments() {
        return List.copyOf(payments);
    }

    public boolean isPaid(Invoice invoice) {
        return payments.stream().anyMatch(p -> p.getInvoice() == invoice);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }
}
