/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistema_hotel;

public class Room {

    int roomNumber;
    String roomType;
    double pricePerNight;
    String status;

    public Room(int roomNumber, String roomType, double pricePerNight) {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Room number must be greater than zero.");
        }
        if (pricePerNight <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than zero.");
        }
        this.roomNumber = roomNumber;
        this.roomType = normalizeType(roomType);
        this.pricePerNight = pricePerNight;
        this.status = "AVAILABLE";
    }

    String normalizeType(String typeInput) {
        if (typeInput == null) {
            throw new IllegalArgumentException("Room type cannot be null.");
        }
        String cleanType = typeInput.trim().toUpperCase();
        if (!cleanType.equals("SINGLE") && !cleanType.equals("DOUBLE") && !cleanType.equals("SUITE")) {
            throw new IllegalArgumentException("Invalid room type. Must be SINGLE, DOUBLE, or SUITE.");
        }
        return cleanType;
    }

    public void changeStatus(String newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        String cleanStatus = newStatus.trim().toUpperCase();
        if (!cleanStatus.equals("AVAILABLE") && !cleanStatus.equals("OCCUPIED") && !cleanStatus.equals("MAINTENANCE")) {
            throw new IllegalArgumentException("Invalid status. Must be AVAILABLE, OCCUPIED, or MAINTENANCE.");
        }
        this.status = cleanStatus;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight <= 0) {
            throw new IllegalArgumentException("Price per night must be greater than zero.");
        }
        this.pricePerNight = pricePerNight;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Room No. " + roomNumber + " | Type: " + roomType + " | Price: $" + pricePerNight + " | Status: " + status;
    }
}