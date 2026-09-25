/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistema_hotel;

import java.util.ArrayList;
import java.util.List;

public class Hotel {

    String name;
    List<Room> rooms;
    List<Guest> guests;
    List<Reservation> reservations;
    List<Invoice> invoices;
    int nextReservationId = 1;
    int nextInvoiceId = 1;

    public Hotel(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Hotel name cannot be empty.");
        }
        this.name = name.trim();
        this.rooms = new ArrayList<>();
        this.guests = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.invoices = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean registerRoom(Room newRoom) {
        if (newRoom == null) {
            throw new IllegalArgumentException("Room to register cannot be null.");
        }
        if (findRoom(newRoom.getRoomNumber()) != null) {
            throw new IllegalArgumentException("A room with number " + newRoom.getRoomNumber() + " already exists.");
        }
        return this.rooms.add(newRoom);
    }

    public Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStatus().equalsIgnoreCase("AVAILABLE")) {
                available.add(room);
            }
        }
        return available;
    }

    public List<Room> getOccupiedRooms() {
        List<Room> occupied = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStatus().equalsIgnoreCase("OCCUPIED")) {
                occupied.add(room);
            }
        }
        return occupied;
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public boolean registerGuest(Guest newGuest) {
        if (newGuest == null) {
            throw new IllegalArgumentException("Guest cannot be null.");
        }
        if (findGuest(newGuest.getIdDocument()) != null) {
            throw new IllegalArgumentException("A guest with ID " + newGuest.getIdDocument() + " already exists.");
        }
        return this.guests.add(newGuest);
    }

    public Guest findGuest(String idDocument) {
        if (idDocument == null) return null;
        for (Guest guest : guests) {
            if (guest.getIdDocument().equalsIgnoreCase(idDocument.trim())) {
                return guest;
            }
        }
        return null;
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    public Reservation createReservation(Guest guest, Room room, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Reservation reservation = new Reservation(nextReservationId++, guest, room, startDate, endDate);
        reservations.add(reservation);
        return reservation;
    }

    public Reservation findReservation(int reservationId) {
        for (Reservation res : reservations) {
            if (res.getReservationId() == reservationId) {
                return res;
            }
        }
        return null;
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public Invoice generateInvoice(Reservation reservation) {
        Invoice invoice = new Invoice(nextInvoiceId++, reservation);
        invoices.add(invoice);
        return invoice;
    }

    public List<Invoice> getAllInvoices() {
        return new ArrayList<>(invoices);
    }
}