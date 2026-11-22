package com.mycompany.system_hotel;

/**
 * Huésped del hotel (RF-05, RF-06).
 */
public class Guest extends Person implements IGuest {

    public Guest(String document, String fullName, String email, String phone) {
        super(document, fullName, email, phone);
    }

    /** HU-06: solo se actualizan los datos de contacto; el documento es inmutable. */
    public void updateContactInfo(String email, String phone) {
        setEmail(email);
        setPhone(phone);
    }

    /**
     * HU-06: actualiza datos personales y de contacto. Se validan los tres valores antes de cambiar
     * cualquiera, para no dejar el huésped a medio actualizar si uno es inválido.
     */
    public void updatePersonalInfo(String fullName, String email, String phone) {
        Guest probe = new Guest(getDocument(), fullName, email, phone);
        setFullName(probe.getFullName());
        setEmail(probe.getEmail());
        setPhone(probe.getPhone());
    }

    @Override
    public String toString() {
        return getFullName() + " | Doc: " + getDocument() + " | Correo: " + getEmail() + " | Tel: " + getPhone();
    }
}
