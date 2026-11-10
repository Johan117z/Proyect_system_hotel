package com.mycompany.system_hotel;

import java.math.BigDecimal;

/**
 * Clase base abstracta de las habitaciones. Concentra las validaciones comunes (NFR-01);
 * cada subclase define su tipo y, si aplica, su forma de calcular el precio.
 */
public abstract class Room implements IRoom {

    private final int number;
    private final int capacity;
    private BigDecimal pricePerNight;
    private RoomStatus status;

    protected Room(int number, int capacity, BigDecimal pricePerNight) {
        if (number <= 0 || number > 65535) {
            throw new IllegalArgumentException("El número de habitación debe estar entre 1 y 65535.");
        }
        if (capacity <= 0 || capacity > 255) {
            throw new IllegalArgumentException("La capacidad debe estar entre 1 y 255 huéspedes.");
        }
        this.number = number;
        this.capacity = capacity;
        this.pricePerNight = validatePrice(pricePerNight);
        this.status = RoomStatus.AVAILABLE;
    }

    private static BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("La tarifa por noche debe ser mayor que cero.");
        }
        return price;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    @Override
    public void setPricePerNight(BigDecimal newPrice) {
        this.pricePerNight = validatePrice(newPrice);
    }

    @Override
    public BigDecimal getExtraServiceFee() {
        return BigDecimal.ZERO;
    }

    @Override
    public RoomStatus getStatus() {
        return status;
    }

    @Override
    public void changeStatus(RoomStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }
        this.status = newStatus;
    }

    @Override
    public boolean isBookable() {
        return status != RoomStatus.MAINTENANCE;
    }

    @Override
    public BigDecimal calculatePrice(long nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException("La estadía debe ser de al menos una noche.");
        }
        return pricePerNight.multiply(BigDecimal.valueOf(nights)).add(getExtraServiceFee());
    }

    @Override
    public String toString() {
        return "Hab. " + number + " | " + getType() + " | Cap: " + capacity
                + " | Tarifa: " + ConsoleUtils.formatCop(pricePerNight)
                + (getExtraServiceFee().signum() > 0 ? " | Cargo extra: " + ConsoleUtils.formatCop(getExtraServiceFee()) : "")
                + " | Estado: " + status;
    }
}
