/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistema_hotel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {

    int reservationId;
    Guest guest;
    Room room;
    LocalDate startDate;
    LocalDate endDate;
    String status;

    public Reservation(int reservationId, Guest guest, Room room, LocalDate startDate, LocalDate endDate) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest cannot be null.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null.");
        }
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        this.reservationId = reservationId;
        this.guest = guest;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "CONFIRMED";
    }

    public long calculateStayDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public void processCheckIn() {
        if (!status.equals("CONFIRMED")) {
            throw new IllegalStateException("Check-in is only allowed for CONFIRMED reservations.");
        }
        this.status = "CHECKED_IN";
        this.room.changeStatus("OCCUPIED");
    }

    public void processCheckOut() {
        if (!status.equals("CHECKED_IN")) {
            throw new IllegalStateException("Check-out is only allowed for CHECKED_IN reservations.");
        }
        this.status = "CHECKED_OUT";
        this.room.changeStatus("AVAILABLE");
    }

    public int getReservationId() {
        return reservationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId + " | Guest: " + guest.getFullName() + " | Room: " + room.getRoomNumber() + " | From: " + startDate + " To: " + endDate + " | Status: " + status;
    }
}