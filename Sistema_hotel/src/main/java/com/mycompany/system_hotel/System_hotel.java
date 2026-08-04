/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.sistema_hotel;

import java.util.List;

public class System_hotel {

    static Hotel hotel = new Hotel("Grand Hotel Continental");

    public static void main(String[] args) {
        loadSeedData();

        int option = -1;
        do {
            showMainMenu();
            option = ConsoleUtils.readInt("Select an option: ");
            System.out.println();

            switch (option) {
                case 1 -> roomManagementMenu();
                case 2 -> guestManagementMenu();
                case 3 -> System.out.println("[DEV 2] Reservation Management under construction...");
                case 4 -> System.out.println("[DEV 2] Check-in under construction...");
                case 5 -> System.out.println("[DEV 2] Check-out under construction...");
                case 6 -> System.out.println("[DEV 2] Billing under construction...");
                case 7 -> showGeneralReport();
                case 0 -> System.out.println("Thank you for using the Hotel Management System!");
                default -> System.err.println("Invalid option. Please try again.");
            }
            System.out.println();
        } while (option != 0);
    }

    static void showMainMenu() {
        System.out.println("============= HOTEL " + hotel.getName().toUpperCase() + " =============");
        System.out.println("1. Room Management");
        System.out.println("2. Guest Management");
        System.out.println("3. Reservation Management");
        System.out.println("4. Check-in");
        System.out.println("5. Check-out");
        System.out.println("6. Billing");
        System.out.println("7. Basic Reports");
        System.out.println("0. Exit");
        System.out.println("===================================================");
    }

    static void roomManagementMenu() {
        int option = -1;
        do {
            System.out.println("--- ROOM MANAGEMENT ---");
            System.out.println("1. Register room");
            System.out.println("2. View all rooms");
            System.out.println("3. View available rooms");
            System.out.println("4. View occupied rooms");
            System.out.println("5. Change room status");
            System.out.println("0. Back to main menu");

            option = ConsoleUtils.readInt("Option: ");

            switch (option) {
                case 1 -> {
                    int num = ConsoleUtils.readInt("Room number: ");
                    String type = ConsoleUtils.readText("Type (Single / Double / Suite): ");
                    double price = ConsoleUtils.readDouble("Price per night: ");
                    try {
                        Room newRoom = new Room(num, type, price);
                        if (hotel.registerRoom(newRoom)) {
                            System.out.println("Room registered successfully.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                }
                case 2 -> displayRoomList(hotel.getAllRooms(), "ALL ROOMS");
                case 3 -> displayRoomList(hotel.getAvailableRooms(), "AVAILABLE ROOMS");
                case 4 -> displayRoomList(hotel.getOccupiedRooms(), "OCCUPIED ROOMS");
                case 5 -> {
                    int num = ConsoleUtils.readInt("Enter room number to modify: ");
                    Room room = hotel.findRoom(num);
                    if (room != null) {
                        System.out.println("Current status: " + room.getStatus());
                        String newStatus = ConsoleUtils.readText("New status (AVAILABLE / OCCUPIED / MAINTENANCE): ");
                        try {
                            room.changeStatus(newStatus);
                            System.out.println("Status updated successfully.");
                        } catch (IllegalArgumentException e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Room not found.");
                    }
                }
                case 0 -> System.out.println("Returning...");
                default -> System.err.println("Invalid option.");
            }
            System.out.println();
        } while (option != 0);
    }

    static void guestManagementMenu() {
        int option = -1;
        do {
            System.out.println("--- GUEST MANAGEMENT ---");
            System.out.println("1. Register guest");
            System.out.println("2. Search guest by ID");
            System.out.println("3. List all guests");
            System.out.println("0. Back to main menu");

            option = ConsoleUtils.readInt("Option: ");

            switch (option) {
                case 1 -> {
                    String doc = ConsoleUtils.readText("ID Document: ");
                    String name = ConsoleUtils.readText("Full Name: ");
                    String email = ConsoleUtils.readText("Email: ");
                    String phone = ConsoleUtils.readText("Phone: ");
                    try {
                        Guest newGuest = new Guest(doc, name, email, phone);
                        if (hotel.registerGuest(newGuest)) {
                            System.out.println("Guest registered successfully.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                }
                case 2 -> {
                    String doc = ConsoleUtils.readText("Enter ID document to search: ");
                    Guest guest = hotel.findGuest(doc);
                    if (guest != null) {
                        System.out.println("Guest found:\n  " + guest);
                    } else {
                        System.err.println("Guest not found.");
                    }
                }
                case 3 -> {
                    List<Guest> list = hotel.getAllGuests();
                    if (list.isEmpty()) {
                        System.out.println("No guests registered.");
                    } else {
                        System.out.println("GUEST LIST:");
                        for (Guest g : list) {
                            System.out.println("  - " + g);
                        }
                    }
                }
                case 0 -> System.out.println("Returning...");
                default -> System.err.println("Invalid option.");
            }
            System.out.println();
        } while (option != 0);
    }

    static void displayRoomList(List<Room> list, String title) {
        if (list.isEmpty()) {
            System.out.println("No rooms to display for '" + title + "'.");
        } else {
            System.out.println(title + ":");
            for (Room r : list) {
                System.out.println("  - " + r);
            }
        }
    }

    static void showGeneralReport() {
        System.out.println("--- GENERAL HOTEL REPORT ---");
        System.out.println("Total Rooms: " + hotel.getAllRooms().size());
        System.out.println("Available Rooms: " + hotel.getAvailableRooms().size());
        System.out.println("Occupied Rooms: " + hotel.getOccupiedRooms().size());
        System.out.println("Total Registered Guests: " + hotel.getAllGuests().size());
    }

    static void loadSeedData() {
        hotel.registerRoom(new Room(101, "Single", 80000.0));
        hotel.registerRoom(new Room(102, "Double", 130000.0));
        hotel.registerRoom(new Room(201, "Suite", 250000.0));
        hotel.registerGuest(new Guest("1098765432", "Alejandro Ruiz", "alejo@email.com", "3001234567"));
    }
}