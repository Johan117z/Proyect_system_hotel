package com.mycompany.system_hotel;

import java.util.regex.Pattern;

/**
 * Datos personales comunes. Las reglas de formato coinciden con las restricciones CHECK de la tabla guests.
 */
public abstract class Person {

    private static final Pattern DOCUMENT = Pattern.compile("^[0-9]{6,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9]{10}$");

    private final String document;
    private String fullName;
    private String email;
    private String phone;

    protected Person(String document, String fullName, String email, String phone) {
        if (document == null || !DOCUMENT.matcher(document.trim()).matches()) {
            throw new IllegalArgumentException("El documento debe tener entre 6 y 10 dígitos.");
        }
        this.document = document.trim();
        setFullName(fullName);
        setEmail(email);
        setPhone(phone);
    }

    public String getDocument() {
        return document;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    protected final void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank() || fullName.trim().length() > 60) {
            throw new IllegalArgumentException("El nombre es obligatorio y admite máximo 60 caracteres.");
        }
        this.fullName = fullName.trim();
    }

    protected final void setEmail(String email) {
        if (email == null || email.trim().length() > 80 || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        this.email = email.trim().toLowerCase();
    }

    protected final void setPhone(String phone) {
        if (phone == null || !PHONE.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException("El teléfono debe tener 10 dígitos.");
        }
        this.phone = phone.trim();
    }
}
