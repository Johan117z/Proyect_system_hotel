package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de consola (alcance de la Fase 2: RS-01). Solo lee datos, llama a Hotel y muestra resultados;
 * no contiene reglas de negocio (NFR-02).
 */
public class System_hotel {

    private static final Hotel hotel = new Hotel("Gran Hotel Continental");

    public static void main(String[] args) {
        loadSeedData();
        int option;
        do {
            showMainMenu();
            option = ConsoleUtils.readInt("Seleccione una opción: ");
            System.out.println();
            switch (option) {
                case 1 -> roomMenu();
                case 2 -> guestMenu();
                case 3 -> reservationMenu();
                case 4 -> run(System_hotel::checkIn);
                case 5 -> run(System_hotel::checkOut);
                case 6 -> showInvoices();
                case 7 -> showReport();
                case 0 -> System.out.println("Gracias por usar el sistema.");
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    /** Ejecuta una acción y muestra cualquier error de validación o de negocio sin cerrar el programa. */
    private static void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | BusinessRuleException | EntityNotFoundException e) {
            System.out.println("  ! Error: " + e.getMessage());
        }
    }

    private static void showMainMenu() {
        System.out.println("============= " + hotel.getName().toUpperCase() + " =============");
        System.out.println("1. Gestión de habitaciones");
        System.out.println("2. Gestión de huéspedes");
        System.out.println("3. Gestión de reservas");
        System.out.println("4. Check-in");
        System.out.println("5. Check-out y facturación");
        System.out.println("6. Facturas emitidas");
        System.out.println("7. Reporte general");
        System.out.println("0. Salir");
        System.out.println("===================================================");
    }

    // ===================== Habitaciones (FR-01 a FR-04) =====================

    private static void roomMenu() {
        int option;
        do {
            System.out.println("--- HABITACIONES ---");
            System.out.println("1. Registrar habitación");
            System.out.println("2. Ver todas");
            System.out.println("3. Ver disponibles");
            System.out.println("4. Ver ocupadas");
            System.out.println("5. Cambiar tarifa");
            System.out.println("6. Cambiar tipo");
            System.out.println("7. Cambiar estado (mantenimiento)");
            System.out.println("8. Eliminar habitación");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(System_hotel::registerRoom);
                case 2 -> printRooms(hotel.getAllRooms(), "TODAS LAS HABITACIONES");
                case 3 -> printRooms(hotel.getRoomsByStatus(RoomStatus.AVAILABLE), "DISPONIBLES");
                case 4 -> printRooms(hotel.getRoomsByStatus(RoomStatus.OCCUPIED), "OCUPADAS");
                case 5 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    BigDecimal price = ConsoleUtils.readMoney("Nueva tarifa por noche: ", false);
                    hotel.updateRoomRate(number, price);
                    System.out.println("Tarifa actualizada.");
                });
                case 6 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    RoomType type = RoomType.fromText(ConsoleUtils.readText("Nuevo tipo (SINGLE / DOUBLE / SUITE): "));
                    int capacity = ConsoleUtils.readInt("Capacidad: ");
                    BigDecimal price = ConsoleUtils.readMoney("Tarifa por noche: ", false);
                    BigDecimal fee = type == RoomType.SUITE
                            ? ConsoleUtils.readMoney("Cargo extra por estadía: ", true) : BigDecimal.ZERO;
                    System.out.println("Actualizada: " + hotel.changeRoomType(number, type, capacity, price, fee));
                });
                case 7 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    RoomStatus status = RoomStatus.fromText(ConsoleUtils.readText("Nuevo estado (AVAILABLE / MAINTENANCE): "));
                    hotel.changeRoomStatus(number, status);
                    System.out.println("Estado actualizado.");
                });
                case 8 -> run(() -> {
                    hotel.deleteRoom(ConsoleUtils.readInt("Número de habitación: "));
                    System.out.println("Habitación eliminada del inventario.");
                });
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    private static void registerRoom() {
        int number = ConsoleUtils.readInt("Número de habitación: ");
        RoomType type = RoomType.fromText(ConsoleUtils.readText("Tipo (SINGLE / DOUBLE / SUITE): "));
        int capacity = ConsoleUtils.readInt("Capacidad: ");
        BigDecimal price = ConsoleUtils.readMoney("Tarifa por noche: ", false);
        BigDecimal fee = type == RoomType.SUITE
                ? ConsoleUtils.readMoney("Cargo extra por estadía: ", true) : BigDecimal.ZERO;
        hotel.registerRoom(RoomFactory.create(type, number, capacity, price, fee));
        System.out.println("Habitación registrada.");
    }

    private static void printRooms(List<IRoom> list, String title) {
        if (list.isEmpty()) {
            System.out.println("No hay habitaciones en: " + title + ".");
            return;
        }
        System.out.println(title + ":");
        list.forEach(r -> System.out.println("  - " + r));
    }

    // ===================== Huéspedes (FR-05, FR-06) =====================

    private static void guestMenu() {
        int option;
        do {
            System.out.println("--- HUÉSPEDES ---");
            System.out.println("1. Registrar huésped");
            System.out.println("2. Buscar por documento");
            System.out.println("3. Actualizar contacto");
            System.out.println("4. Ver todos");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento: ");
                    String name = ConsoleUtils.readText("Nombre completo: ");
                    String email = ConsoleUtils.readText("Correo: ");
                    String phone = ConsoleUtils.readText("Teléfono: ");
                    hotel.registerGuest(new Guest(doc, name, email, phone));
                    System.out.println("Huésped registrado.");
                });
                case 2 -> run(() -> System.out.println("  " + hotel.findGuest(ConsoleUtils.readText("Documento: "))));
                case 3 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento: ");
                    hotel.findGuest(doc);
                    String email = ConsoleUtils.readText("Nuevo correo: ");
                    String phone = ConsoleUtils.readText("Nuevo teléfono: ");
                    hotel.updateGuestContact(doc, email, phone);
                    System.out.println("Contacto actualizado.");
                });
                case 4 -> {
                    List<Guest> list = hotel.getAllGuests();
                    if (list.isEmpty()) {
                        System.out.println("No hay huéspedes registrados.");
                    } else {
                        list.forEach(g -> System.out.println("  - " + g));
                    }
                }
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    // ===================== Reservas (FR-07, FR-10) =====================

    private static void reservationMenu() {
        int option;
        do {
            System.out.println("--- RESERVAS ---");
            System.out.println("1. Crear reserva");
            System.out.println("2. Ver todas");
            System.out.println("3. Cancelar reserva");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento del huésped: ");
                    int room = ConsoleUtils.readInt("Número de habitación: ");
                    LocalDate in = ConsoleUtils.readDate("Fecha de entrada");
                    LocalDate out = ConsoleUtils.readDate("Fecha de salida");
                    System.out.println("Reserva creada: " + hotel.createReservation(doc, room, in, out));
                });
                case 2 -> {
                    List<Reservation> list = hotel.getAllReservations();
                    if (list.isEmpty()) {
                        System.out.println("No hay reservas.");
                    } else {
                        list.forEach(r -> System.out.println("  - " + r));
                    }
                }
                case 3 -> run(() -> System.out.println("Reserva cancelada: "
                        + hotel.cancelReservation(ConsoleUtils.readText("Código de reserva (RES-AAAA-NNN): "))));
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    // ===================== Operación (FR-08, FR-09) =====================

    private static void checkIn() {
        Reservation r = hotel.checkIn(ConsoleUtils.readText("Código de reserva: "));
        System.out.println("Check-in realizado. Habitación " + r.getRoom().getNumber() + " ahora OCCUPIED.");
    }

    private static void checkOut() {
        String code = ConsoleUtils.readText("Código de reserva: ");
        hotel.findReservation(code);
        BigDecimal extras = ConsoleUtils.readMoney("Servicios adicionales consumidos (0 si no hay): ", true);
        Invoice invoice = hotel.checkOut(code, extras);
        System.out.println("\n--- FACTURA GENERADA ---\n" + invoice);
    }

    private static void showInvoices() {
        List<Invoice> list = hotel.getAllInvoices();
        if (list.isEmpty()) {
            System.out.println("No hay facturas emitidas.");
            return;
        }
        list.forEach(i -> System.out.println("-----------------------------------\n" + i));
        System.out.println("-----------------------------------");
    }

    private static void showReport() {
        System.out.println("--- REPORTE GENERAL ---");
        System.out.println("Habitaciones: " + hotel.getAllRooms().size());
        System.out.println("Disponibles: " + hotel.getRoomsByStatus(RoomStatus.AVAILABLE).size());
        System.out.println("Ocupadas: " + hotel.getRoomsByStatus(RoomStatus.OCCUPIED).size());
        System.out.println("En mantenimiento: " + hotel.getRoomsByStatus(RoomStatus.MAINTENANCE).size());
        System.out.println("Huéspedes: " + hotel.getAllGuests().size());
        System.out.println("Reservas: " + hotel.getAllReservations().size());
        BigDecimal billed = hotel.getAllInvoices().stream()
                .map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Facturas: " + hotel.getAllInvoices().size() + " | Total facturado: " + ConsoleUtils.formatCop(billed));
    }

    /** Mismos datos de prueba que script_hotel_db.sql. */
    private static void loadSeedData() {
        hotel.registerRoom(RoomFactory.create(RoomType.SINGLE, 101, 1, new BigDecimal("120000"), null));
        hotel.registerRoom(RoomFactory.create(RoomType.DOUBLE, 102, 2, new BigDecimal("200000"), null));
        hotel.registerRoom(RoomFactory.create(RoomType.SUITE, 201, 4, new BigDecimal("350000"), new BigDecimal("50000")));
        hotel.registerRoom(RoomFactory.create(RoomType.SINGLE, 202, 1, new BigDecimal("120000"), null));
        hotel.changeRoomStatus(202, RoomStatus.MAINTENANCE);

        hotel.registerGuest(new Guest("1075234567", "Carlos Pérez", "carlos.perez@example.com", "3101234567"));
        hotel.registerGuest(new Guest("1075987654", "Ana María Gómez", "ana.gomez@example.com", "3209876543"));
        hotel.registerGuest(new Guest("1076543210", "Luis Fernando Torres", "luis.torres@example.com", "3154567890"));
    }
}
