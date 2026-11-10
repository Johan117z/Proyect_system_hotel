package com.mycompany.system_hotel;

/**
 * Contrato mínimo de huésped que necesitan Reservation e Invoice.
 */
public interface IGuest {

    String getDocument();

    String getFullName();
}
