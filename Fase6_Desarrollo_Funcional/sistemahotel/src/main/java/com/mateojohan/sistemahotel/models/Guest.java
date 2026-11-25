package com.mateojohan.sistemahotel.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Huésped del hotel (HU-05 a HU-08) -> tabla guests.
 * La eliminación es lógica (active = false) para conservar su historial (HU-07).
 */
@Entity
@Table(name = "guests")
public class Guest extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Guest() {
    }

    public Guest(String document, String fullName, String email, String phone) {
        super(document, fullName, email, phone);
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** HU-06: solo datos de contacto; el documento es inmutable. */
    public void updateContactInfo(String email, String phone) {
        setEmail(email);
        setPhone(phone);
    }

    /** HU-06: datos personales y de contacto. */
    public void updatePersonalInfo(String fullName, String email, String phone) {
        setFullName(fullName);
        updateContactInfo(email, phone);
    }

    /** HU-07: baja lógica. */
    public void deactivate() {
        active = Boolean.FALSE;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
