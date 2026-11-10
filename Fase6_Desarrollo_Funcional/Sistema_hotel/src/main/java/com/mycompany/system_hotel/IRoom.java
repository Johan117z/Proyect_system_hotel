package com.mycompany.system_hotel;

import java.math.BigDecimal;

/**
 * Contrato de habitación (diagrama de clases, Fase 3). Hotel y Reservation dependen
 * de esta abstracción y no de las clases concretas (Principio de Inversión de Dependencias).
 */
public interface IRoom {

    int getNumber();

    RoomType getType();

    int getCapacity();

    BigDecimal getPricePerNight();

    /** Cargo fijo por estadía. Solo la Suite lo usa; las demás devuelven 0. */
    BigDecimal getExtraServiceFee();

    RoomStatus getStatus();

    void changeStatus(RoomStatus newStatus);

    void setPricePerNight(BigDecimal newPrice);

    /** Una habitación en mantenimiento no se puede reservar. */
    boolean isBookable();

    /** Valor del alojamiento: noches × tarifa + cargo extra del tipo. */
    BigDecimal calculatePrice(long nights);
}
