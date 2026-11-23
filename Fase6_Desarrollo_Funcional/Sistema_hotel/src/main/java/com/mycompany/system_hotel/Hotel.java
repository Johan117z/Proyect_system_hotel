package com.mycompany.system_hotel;

/**
 * Punto de entrada al modelo del hotel. Su única responsabilidad es crear el almacenamiento y los
 * servicios y conectarlos; las reglas de negocio viven en cada servicio (alta cohesión):
 * <ul>
 *   <li>{@link RoomService}: habitaciones (HU-01 a HU-04)</li>
 *   <li>{@link GuestService}: huéspedes (HU-05 a HU-08)</li>
 *   <li>{@link ReservationService}: reservas, disponibilidad, check-in y check-out (HU-09 a HU-13)</li>
 *   <li>{@link BillingService}: servicios adicionales, factura y pago (HU-14)</li>
 * </ul>
 * Los servicios no se llaman entre sí: comparten un único {@link HotelStore}, así no hay dependencias circulares.
 */
public class Hotel {

    private final String name;
    private final RoomService rooms;
    private final GuestService guests;
    private final ReservationService reservations;
    private final BillingService billing;

    public Hotel(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del hotel es obligatorio.");
        }
        this.name = name.trim();
        HotelStore store = new HotelStore();
        this.rooms = new RoomService(store);
        this.guests = new GuestService(store);
        this.reservations = new ReservationService(store);
        this.billing = new BillingService(store);
    }

    public String getName() {
        return name;
    }

    public RoomService rooms() {
        return rooms;
    }

    public GuestService guests() {
        return guests;
    }

    public ReservationService reservations() {
        return reservations;
    }

    public BillingService billing() {
        return billing;
    }
}
