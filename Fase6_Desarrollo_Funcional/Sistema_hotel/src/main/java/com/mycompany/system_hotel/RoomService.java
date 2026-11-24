package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.util.List;

/**
 * Épica 1 — Habitaciones (HU-01 a HU-04): reglas para registrar, modificar, eliminar y listar habitaciones.
 */
public class RoomService {

    private final HotelStore store;

    public RoomService(HotelStore store) {
        this.store = store;
    }

    /** HU-01 / RN-01: el número de habitación es único. */
    public void register(IRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("La habitación es obligatoria.");
        }
        if (store.roomNumberExists(room.getNumber())) {
            throw new BusinessRuleException("RN-01", "El número de habitación ya se encuentra registrado.");
        }
        store.addRoom(room);
    }

    /** HU-02: si el número no existe, informa que la habitación no fue encontrada. */
    public IRoom find(int number) {
        return store.findRoom(number);
    }

    /** HU-04 */
    public List<IRoom> getAll() {
        return store.getActiveRooms();
    }

    /** HU-04 */
    public List<IRoom> getByStatus(RoomStatus status) {
        return store.getActiveRooms().stream().filter(r -> r.getStatus() == status).toList();
    }

    /** HU-02: cambio de tarifa. No afecta facturas emitidas (RN-12) porque Invoice copia la tarifa. */
    public void updateRate(int number, BigDecimal newPrice) {
        store.findRoom(number).setPricePerNight(newPrice);
    }

    /**
     * HU-02: cambio de tipo y capacidad. Como el tipo define la subclase, se reemplaza el objeto.
     * Solo se permite si la habitación no tiene reservas pendientes o activas (RN-14).
     */
    public IRoom changeType(int number, RoomType newType, int capacity,
                            BigDecimal pricePerNight, BigDecimal extraServiceFee) {
        IRoom current = store.findRoom(number);
        if (hasBlockingReservations(current)) {
            throw new BusinessRuleException("RN-14",
                    "No se puede cambiar el tipo: la habitación " + number + " tiene reservas pendientes o activas.");
        }
        Room replacement = RoomFactory.create(newType, number, capacity, pricePerNight, extraServiceFee);
        replacement.changeStatus(current.getStatus());
        store.replaceRoom(current, replacement);
        return replacement;
    }

    /**
     * Cambio manual de estado: AVAILABLE ↔ MAINTENANCE (por ejemplo, habilitar una habitación después
     * de la limpieza). OCCUPIED solo se asigna con el check-in y se libera con el check-out (RN-15).
     */
    public void changeStatus(int number, RoomStatus newStatus) {
        IRoom room = store.findRoom(number);
        if (newStatus == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("RN-15", "El estado OCCUPIED solo se asigna al hacer check-in.");
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new BusinessRuleException("RN-15", "La habitación está ocupada; se libera con el check-out.");
        }
        room.changeStatus(newStatus);
    }

    /** HU-03 / RN-02: baja lógica, solo si no está ocupada ni tiene reservas futuras. Conserva el historial. */
    public void delete(int number) {
        IRoom room = store.findRoom(number);
        if (hasBlockingReservations(room)) {
            throw new BusinessRuleException("RN-02",
                    "No se puede eliminar: la habitación " + number + " está ocupada o tiene reservas futuras asociadas.");
        }
        store.retireRoom(room);
    }

    private boolean hasBlockingReservations(IRoom room) {
        return store.getReservations().stream()
                .anyMatch(r -> r.getRoom() == room && r.getStatus().blocksRoom());
    }
}
