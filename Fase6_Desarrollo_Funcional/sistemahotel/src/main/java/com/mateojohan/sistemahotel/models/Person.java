package com.mateojohan.sistemahotel.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Datos personales comunes (clase Person del diagrama UML). No es tabla propia:
 * sus columnas se heredan en guests. El formato se valida en los DTO de entrada.
 */
@MappedSuperclass
public abstract class Person {

    @Column(name = "document", nullable = false, unique = true, length = 10, updatable = false)
    private String document;

    @Column(name = "full_name", nullable = false, length = 60)
    private String fullName;

    @Column(name = "email", nullable = false, length = 80)
    private String email;

    @Column(name = "phone", nullable = false, length = 10)
    private String phone;

    protected Person() {
    }

    protected Person(String document, String fullName, String email, String phone) {
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
        this.fullName = fullName.trim();
    }

    protected final void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    protected final void setPhone(String phone) {
        this.phone = phone.trim();
    }
}
