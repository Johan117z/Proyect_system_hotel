package com.mycompany.system_hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de consola (RS-01). Solo lee datos, llama a Hotel y muestra resultados;
 * no contiene reglas de negocio (RNF01); las reglas están en los servicios de Hotel. Cada menú tiene opciones numeradas y "0. Volver" (RNF03).
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
                case 4 -> stayMenu();
                case 5 -> billingMenu();
                case 6 -> showReport();
                case 0 -> System.out.println("Gracias por usar el sistema.");
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    /** Ejecuta una acción y muestra cualquier error de validación o de negocio sin cerrar el programa (RNF04). */
    private static void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | BusinessRuleException | EntityNotFoundException e) {
            System.out.println("  ! Error: " + e.getMessage());
        }
    }

    private static void showMainMenu() {
        System.out.println("============= " + hotel.getName().toUpperCase() + " =============");
        System.out.println("1. Habitaciones");
        System.out.println("2. Huéspedes");
        System.out.println("3. Reservas y disponibilidad");
        System.out.println("4. Estadía (check-in, servicios, check-out)");
        System.out.println("5. Facturación y pagos");
        System.out.println("6. Reporte general");
        System.out.println("0. Salir");
        System.out.println("===================================================");
    }

    // ===================== Épica 1: Habitaciones (HU-01 a HU-04) =====================

    private static void roomMenu() {
        int option;
        do {
            System.out.println("--- HABITACIONES ---");
            System.out.println("1. Registrar habitación");
            System.out.println("2. Listar todas");
            System.out.println("3. Listar disponibles");
            System.out.println("4. Listar ocupadas");
            System.out.println("5. Listar en mantenimiento/limpieza");
            System.out.println("6. Modificar tarifa");
            System.out.println("7. Modificar tipo y capacidad");
            System.out.println("8. Cambiar estado (habilitar o enviar a mantenimiento)");
            System.out.println("9. Eliminar habitación");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(System_hotel::registerRoom);
                case 2 -> printList(hotel.rooms().getAll(), "No hay habitaciones registradas.");
                case 3 -> printList(hotel.rooms().getByStatus(RoomStatus.AVAILABLE), "No hay habitaciones disponibles.");
                case 4 -> printList(hotel.rooms().getByStatus(RoomStatus.OCCUPIED), "No hay habitaciones ocupadas.");
                case 5 -> printList(hotel.rooms().getByStatus(RoomStatus.MAINTENANCE), "No hay habitaciones en mantenimiento.");
                case 6 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    hotel.rooms().find(number);
                    BigDecimal price = ConsoleUtils.readMoney("Nueva tarifa por noche: ", false);
                    hotel.rooms().updateRate(number, price);
                    System.out.println("Tarifa actualizada.");
                });
                case 7 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    hotel.rooms().find(number);
                    RoomType type = RoomType.fromText(ConsoleUtils.readText("Nuevo tipo (SINGLE / DOUBLE / SUITE): "));
                    int capacity = ConsoleUtils.readInt("Capacidad: ");
                    BigDecimal price = ConsoleUtils.readMoney("Tarifa por noche: ", false);
                    BigDecimal fee = type == RoomType.SUITE
                            ? ConsoleUtils.readMoney("Cargo extra por estadía: ", true) : BigDecimal.ZERO;
                    System.out.println("Actualizada: " + hotel.rooms().changeType(number, type, capacity, price, fee));
                });
                case 8 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    hotel.rooms().find(number);
                    RoomStatus status = RoomStatus.fromText(ConsoleUtils.readText("Nuevo estado (AVAILABLE / MAINTENANCE): "));
                    hotel.rooms().changeStatus(number, status);
                    System.out.println("Estado actualizado.");
                });
                case 9 -> run(() -> {
                    int number = ConsoleUtils.readInt("Número de habitación: ");
                    hotel.rooms().find(number);
                    if (ConsoleUtils.confirm("¿Confirma la eliminación de la habitación " + number + "?")) {
                        hotel.rooms().delete(number);
                        System.out.println("Habitación eliminada del inventario.");
                    } else {
                        System.out.println("Operación cancelada.");
                    }
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
        hotel.rooms().register(RoomFactory.create(type, number, capacity, price, fee));
        System.out.println("Habitación registrada en estado AVAILABLE.");
    }

    // ===================== Épica 2: Huéspedes (HU-05 a HU-08) =====================

    private static void guestMenu() {
        int option;
        do {
            System.out.println("--- HUÉSPEDES ---");
            System.out.println("1. Registrar huésped");
            System.out.println("2. Buscar por nombre, apellido o documento");
            System.out.println("3. Actualizar datos");
            System.out.println("4. Eliminar huésped");
            System.out.println("5. Listar todos");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento (6 a 10 dígitos): ");
                    String name = ConsoleUtils.readText("Nombre completo: ");
                    String email = ConsoleUtils.readText("Correo: ");
                    String phone = ConsoleUtils.readText("Teléfono (10 dígitos): ");
                    hotel.guests().register(new Guest(doc, name, email, phone));
                    System.out.println("Huésped registrado.");
                });
                case 2 -> run(() -> printList(
                        hotel.guests().search(ConsoleUtils.readText("Nombre, apellido o documento: ")),
                        "No se encontraron resultados."));
                case 3 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento: ");
                    Guest guest = hotel.guests().find(doc);
                    System.out.println("  Actual: " + guest);
                    String name = ConsoleUtils.readText("Nombre completo: ");
                    String email = ConsoleUtils.readText("Correo: ");
                    String phone = ConsoleUtils.readText("Teléfono: ");
                    hotel.guests().update(doc, name, email, phone);
                    System.out.println("Datos actualizados.");
                });
                case 4 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento: ");
                    Guest guest = hotel.guests().find(doc);
                    if (ConsoleUtils.confirm("¿Confirma la eliminación de " + guest.getFullName() + "?")) {
                        hotel.guests().delete(doc);
                        System.out.println("Huésped eliminado.");
                    } else {
                        System.out.println("Operación cancelada.");
                    }
                });
                case 5 -> printList(hotel.guests().getAll(), "No hay huéspedes registrados.");
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    // ===================== Épica 3: Reservas (HU-09 a HU-11) =====================

    private static void reservationMenu() {
        int option;
        do {
            System.out.println("--- RESERVAS Y DISPONIBILIDAD ---");
            System.out.println("1. Crear reserva");
            System.out.println("2. Consultar disponibilidad por fechas");
            System.out.println("3. Listar reservas");
            System.out.println("4. Cancelar reserva");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(() -> {
                    String doc = ConsoleUtils.readText("Documento del huésped: ");
                    hotel.guests().find(doc);
                    int room = ConsoleUtils.readInt("Número de habitación: ");
                    LocalDate in = ConsoleUtils.readDate("Fecha de entrada");
                    LocalDate out = ConsoleUtils.readDate("Fecha de salida");
                    System.out.println("Reserva creada: " + hotel.reservations().create(doc, room, in, out));
                });
                case 2 -> run(() -> {
                    LocalDate in = ConsoleUtils.readDate("Fecha de entrada");
                    LocalDate out = ConsoleUtils.readDate("Fecha de salida");
                    printList(hotel.reservations().getAvailableRooms(in, out),
                            "No hay habitaciones disponibles en el rango de fechas seleccionado.");
                });
                case 3 -> printList(hotel.reservations().getAll(), "No hay reservas registradas.");
                case 4 -> run(() -> {
                    String code = ConsoleUtils.readText("Código de reserva (RES-AAAA-NNN): ");
                    Reservation reservation = hotel.reservations().find(code);
                    if (reservation.getStatus() != ReservationStatus.PENDING) {
                        hotel.reservations().cancel(code);   // informa por qué no se puede cancelar (HU-10)
                    }
                    if (ConsoleUtils.confirm("¿Confirma la cancelación de " + reservation.getReservationCode() + "?")) {
                        System.out.println("Reserva cancelada: " + hotel.reservations().cancel(code));
                    } else {
                        System.out.println("Operación cancelada.");
                    }
                });
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    // ===================== Épica 4: Estadía (HU-12, HU-13) =====================

    private static void stayMenu() {
        int option;
        do {
            System.out.println("--- ESTADÍA ---");
            System.out.println("1. Check-in");
            System.out.println("2. Registrar servicio adicional");
            System.out.println("3. Ver cuenta de una estadía");
            System.out.println("4. Check-out");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(() -> {
                    Reservation r = hotel.reservations().checkIn(ConsoleUtils.readText("Código de reserva: "));
                    System.out.println("Check-in realizado. Habitación " + r.getRoom().getNumber() + " ahora OCCUPIED.");
                });
                case 2 -> run(() -> {
                    String code = ConsoleUtils.readText("Código de reserva: ");
                    hotel.reservations().find(code);
                    String description = ConsoleUtils.readText("Descripción (ej. Minibar): ");
                    BigDecimal amount = ConsoleUtils.readMoney("Valor: ", false);
                    System.out.println("Servicio registrado: " + hotel.billing().addService(code, description, amount));
                });
                case 3 -> run(() -> showAccount(hotel.reservations().find(ConsoleUtils.readText("Código de reserva: "))));
                case 4 -> run(() -> {
                    Reservation r = hotel.reservations().checkOut(ConsoleUtils.readText("Código de reserva: "));
                    System.out.println("Check-out realizado. Habitación " + r.getRoom().getNumber()
                            + " en MAINTENANCE (mantenimiento/limpieza).");
                });
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    private static void showAccount(Reservation reservation) {
        System.out.println("  " + reservation);
        List<AdditionalService> services = reservation.getServices();
        if (services.isEmpty()) {
            System.out.println("  Sin servicios adicionales.");
        } else {
            services.forEach(s -> System.out.println("  · " + s));
        }
        System.out.println("  Servicios: " + ConsoleUtils.formatCop(reservation.getServicesTotal()));
        hotel.billing().findInvoiceFor(reservation).ifPresentOrElse(
                i -> System.out.println("  Factura No. " + i.getInvoiceId() + " por "
                        + ConsoleUtils.formatCop(i.getTotalAmount()) + " — "
                        + (hotel.billing().isPaid(i) ? "PAGADA" : "PENDIENTE DE PAGO")),
                () -> System.out.println("  Sin factura emitida."));
    }

    // ===================== Épica 5: Facturación (HU-14) =====================

    private static void billingMenu() {
        int option;
        do {
            System.out.println("--- FACTURACIÓN Y PAGOS ---");
            System.out.println("1. Generar factura de una estadía");
            System.out.println("2. Registrar pago de una factura");
            System.out.println("3. Listar facturas");
            System.out.println("0. Volver");
            option = ConsoleUtils.readInt("Opción: ");
            switch (option) {
                case 1 -> run(() -> {
                    Invoice invoice = hotel.billing().generateInvoice(ConsoleUtils.readText("Código de reserva: "));
                    System.out.println("\n--- FACTURA GENERADA ---\n" + invoice);
                });
                case 2 -> run(() -> {
                    int id = ConsoleUtils.readInt("Número de factura: ");
                    Invoice invoice = hotel.billing().findInvoice(id);
                    if (hotel.billing().isPaid(invoice)) {
                        System.out.println("  La factura No. " + id + " ya está pagada.");
                        return;
                    }
                    System.out.println("  Total a pagar: " + ConsoleUtils.formatCop(invoice.getTotalAmount()));
                    PaymentMethod method = PaymentMethod.fromText(
                            ConsoleUtils.readText("Medio de pago (Efectivo / Tarjeta / Transferencia): "));
                    System.out.println(hotel.billing().registerPayment(id, method) + ". Cuenta saldada.");
                });
                case 3 -> {
                    List<Invoice> list = hotel.billing().getAllInvoices();
                    if (list.isEmpty()) {
                        System.out.println("No hay facturas emitidas.");
                    } else {
                        list.forEach(i -> System.out.println("-----------------------------------\n" + i
                                + "\nEstado: " + (hotel.billing().isPaid(i) ? "PAGADA" : "PENDIENTE DE PAGO")));
                        System.out.println("-----------------------------------");
                    }
                }
                case 0 -> { }
                default -> System.out.println("  ! Opción inválida.");
            }
            System.out.println();
        } while (option != 0);
    }

    // ===================== Reporte =====================

    private static void showReport() {
        System.out.println("--- REPORTE GENERAL ---");
        System.out.println("Habitaciones: " + hotel.rooms().getAll().size());
        System.out.println("  Disponibles: " + hotel.rooms().getByStatus(RoomStatus.AVAILABLE).size());
        System.out.println("  Ocupadas: " + hotel.rooms().getByStatus(RoomStatus.OCCUPIED).size());
        System.out.println("  En mantenimiento/limpieza: " + hotel.rooms().getByStatus(RoomStatus.MAINTENANCE).size());
        System.out.println("Huéspedes: " + hotel.guests().getAll().size());
        System.out.println("Reservas: " + hotel.reservations().getAll().size());
        BigDecimal billed = hotel.billing().getAllInvoices().stream()
                .map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collected = hotel.billing().getAllPayments().stream()
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Facturas: " + hotel.billing().getAllInvoices().size() + " | Facturado: " + ConsoleUtils.formatCop(billed)
                + " | Cobrado: " + ConsoleUtils.formatCop(collected));
    }

    private static void printList(List<?> list, String emptyMessage) {
        if (list.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        list.forEach(item -> System.out.println("  - " + item));
    }

    /** Mismas habitaciones y huéspedes que script_hotel_db.sql. */
    private static void loadSeedData() {
        hotel.rooms().register(RoomFactory.create(RoomType.SINGLE, 101, 1, new BigDecimal("120000"), null));
        hotel.rooms().register(RoomFactory.create(RoomType.DOUBLE, 102, 2, new BigDecimal("200000"), null));
        hotel.rooms().register(RoomFactory.create(RoomType.SUITE, 201, 4, new BigDecimal("350000"), new BigDecimal("50000")));
        hotel.rooms().register(RoomFactory.create(RoomType.SINGLE, 202, 1, new BigDecimal("120000"), null));
        hotel.rooms().changeStatus(202, RoomStatus.MAINTENANCE);

        hotel.guests().register(new Guest("1075234567", "Carlos Pérez", "carlos.perez@example.com", "3101234567"));
        hotel.guests().register(new Guest("1075987654", "Ana María Gómez", "ana.gomez@example.com", "3209876543"));
        hotel.guests().register(new Guest("1076543210", "Luis Fernando Torres", "luis.torres@example.com", "3154567890"));
    }
}
