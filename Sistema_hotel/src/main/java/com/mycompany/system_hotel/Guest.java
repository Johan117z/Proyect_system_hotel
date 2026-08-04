/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistema_hotel;

public class Guest {

    String idDocument;
    String fullName;
    String email;
    String phone;

    public Guest(String idDocument, String fullName, String email, String phone) {
        if (idDocument == null || idDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("ID document cannot be empty.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email address format is invalid.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }

        this.idDocument = idDocument.trim();
        this.fullName = fullName.trim();
        this.email = email.trim();
        this.phone = phone.trim();
    }

    public String getIdDocument() {
        return idDocument;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email address format is invalid.");
        }
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phone = phone.trim();
    }

    @Override
    public String toString() {
        return "Guest: " + fullName + " | ID: " + idDocument + " | Email: " + email + " | Phone: " + phone;
    }
}