package com.mycompany.system_hotel;

/**
 * Huésped del hotel (FR-05, FR-06).
 */
public class Guest extends Person implements IGuest {

    public Guest(String document, String fullName, String email, String phone) {
        super(document, fullName, email, phone);
    }

    /** FR-06: solo se actualizan los datos de contacto; el documento es inmutable. */
    public void updateContactInfo(String email, String phone) {
        setEmail(email);
        setPhone(phone);
    }

    @Override
    public String toString() {
        return getFullName() + " | Doc: " + getDocument() + " | Correo: " + getEmail() + " | Tel: " + getPhone();
    }
}
