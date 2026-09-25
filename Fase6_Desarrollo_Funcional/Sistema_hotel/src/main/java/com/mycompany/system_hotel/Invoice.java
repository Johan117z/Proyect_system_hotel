/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistema_hotel;

import java.time.LocalDate;

public class Invoice {

    int invoiceId;
    Reservation reservation;
    double totalAmount;
    LocalDate issueDate;

    public Invoice(int invoiceId, Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null.");
        }
        this.invoiceId = invoiceId;
        this.reservation = reservation;
        this.issueDate = LocalDate.now();
        this.totalAmount = calculateTotalAmount();
    }

    double calculateTotalAmount() {
        long days = reservation.calculateStayDays();
        double pricePerNight = reservation.getRoom().getPricePerNight();
        return days * pricePerNight;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "INVOICE No. " + invoiceId + "\n" +
               "Issue Date: " + issueDate + "\n" +
               "Guest: " + reservation.getGuest().getFullName() + "\n" +
               "Room: " + reservation.getRoom().getRoomNumber() + " (" + reservation.getRoom().getRoomType() + ")\n" +
               "Nights Stayed: " + reservation.calculateStayDays() + "\n" +
               "Total Amount: $" + totalAmount;
    }
}