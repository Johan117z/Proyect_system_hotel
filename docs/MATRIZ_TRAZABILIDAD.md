# Matriz de trazabilidad — System_hotel v2.1

Relaciona cada historia de usuario (Fase 1) con su requisito del SRS v2.0 (Fase 2), sus reglas de negocio, su caso de uso (Fase 3), su implementación Java (Fase 6), su estructura en la base de datos (Fase 4) y su pantalla en el prototipo (Fase 5). El objetivo es que todos los artefactos describan **el mismo sistema**.

**Base oficial:** los documentos formales en PDF de las Fases 1 y 2 (14 historias, RF-01 a RF-14, RN-01 a RN-11). La v2.0 del SRS agrega RN-12 a RN-15.

## 1. Modelo canónico

| Concepto | Valor único en SRS, UML, BD, Java y prototipo |
| :--- | :--- |
| Estados de habitación | `AVAILABLE` (Disponible) / `OCCUPIED` (Ocupada) / `MAINTENANCE` (Mantenimiento/Limpieza) |
| Estados de reserva | `PENDING` → `ACTIVE` (check-in) → `COMPLETED` (check-out); `PENDING` → `CANCELLED` |
| Tipos de habitación | `SINGLE` / `DOUBLE` / `SUITE` → subclases de `Room` |
| Habitación después del check-out | `MAINTENANCE` (RN-10); el recepcionista la habilita como `AVAILABLE` |
| Código de reserva | `RES-AAAA-NNN` (`reservations.reservation_code`) |
| Flujo de cobro | Servicios durante la estadía → factura (reserva `ACTIVE`) → pago total → check-out |
| Total de la factura | noches de la reserva × tarifa + cargo extra Suite + servicios adicionales (RN-11) |
| Factura | Inmutable, una por estadía; no se anula ni se elimina (RN-12) |
| Dinero | `BigDecimal` en Java, `DECIMAL` en BD; mostrado como `$ 1.830.000 COP` |
| Documento / teléfono | 6 a 10 dígitos / 10 dígitos |
| Eliminación de habitaciones y huéspedes | Lógica (`is_active`), conserva el historial |

## 2. Historias y requisitos funcionales

| HU | RF | Reglas | CU | Java | Base de datos | Prototipo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| HU-01 Registrar habitación | RF-01 | RN-01 | CU-01 | `Hotel.registerRoom`, `RoomFactory.create`, validaciones de `Room` / `SuiteRoom` | `rooms`: `uq_rooms_number`, `ck_rooms_*` | Habitaciones → Nueva |
| HU-02 Modificar habitación | RF-02 | RN-14 | CU-01 | `Hotel.updateRoomRate`, `Hotel.changeRoomType` | `rooms.price_per_night`, `rooms.type`, `rooms.capacity` | Habitaciones → Editar |
| HU-03 Eliminar habitación | RF-03 | RN-02 | CU-01 | `Hotel.deleteRoom` (baja lógica) | `rooms.is_active`; FK `RESTRICT` | Habitaciones → Eliminar |
| HU-04 Listar habitaciones | RF-04 | — | CU-01 | `Hotel.getAllRooms`, `Hotel.getRoomsByStatus` | `rooms.status` | Panel general, Habitaciones |
| HU-05 Registrar huésped | RF-05 | RN-03 | — | `Hotel.registerGuest`, validaciones de `Person` | `guests`: `uq_guests_document`, `ck_guests_*` | Huéspedes → Registrar |
| HU-06 Actualizar huésped | RF-06 | — | — | `Hotel.updateGuest`, `Guest.updatePersonalInfo` | `guests.full_name`, `email`, `phone` | Huéspedes → Editar |
| HU-07 Eliminar huésped | RF-07 | RN-04 | — | `Hotel.deleteGuest` (baja lógica) | `guests.is_active`; consulta de referencia RN-04 | Huéspedes → Eliminar |
| HU-08 Consultar huéspedes | RF-08 | — | — | `Hotel.searchGuests` (sin mayúsculas ni tildes) | — | Huéspedes → Buscar |
| HU-09 Crear reserva | RF-09 | RN-05, RN-13 | CU-02 | `Hotel.createReservation`, `Reservation.overlaps` | `reservations`, `ck_reservations_dates`, `ix_reservations_overlap` | Reservas → Nueva |
| HU-10 Cancelar reserva | RF-10 | RN-06 | — | `Hotel.cancelReservation`, `Reservation.cancel` | `reservations.status = CANCELLED` | Reservas → Cancelar |
| HU-11 Consultar disponibilidad | RF-11 | RN-07 | CU-02 | `Hotel.getAvailableRooms` | Consulta de referencia RN-05/RN-07 | Disponibilidad |
| HU-12 Check-in | RF-12 | RN-08, RN-15 | CU-03 | `Hotel.checkIn`, `Reservation.checkIn(today)` | `reservations.status = ACTIVE`, `rooms.status = OCCUPIED` | Estadías → Check-in |
| HU-13 Check-out | RF-13 | RN-09, RN-10, RN-15 | CU-03 | `Hotel.checkOut`, `Reservation.checkOut` | `payments` (cuenta saldada), `rooms.status = MAINTENANCE` | Estadías → Check-out |
| HU-14 Generar factura | RF-14 | RN-11, RN-12 | CU-03 | `Hotel.addService`, `Hotel.generateInvoice`, `Hotel.registerPayment`, `Invoice`, `Payment` | `additional_services`, `invoices`, `payments`, `ck_invoices_total`, triggers | Estadías → Servicio / Factura / Cobrar; Facturación |

## 3. Reglas de negocio

| Regla | Descripción | Java | Base de datos |
| :--- | :--- | :--- | :--- |
| RN-01 | Número de habitación único | `Hotel.registerRoom` | `uq_rooms_number` |
| RN-02 | No eliminar habitación ocupada o con reservas futuras | `Hotel.deleteRoom` | FK `RESTRICT` |
| RN-03 | Documento de huésped único | `Hotel.registerGuest` | `uq_guests_document` |
| RN-04 | No eliminar huésped con hospedaje activo, reservas vigentes o facturas sin pagar | `Hotel.deleteGuest` | Consulta de referencia al final del script |
| RN-05 | Sin cruce de fechas; salida exclusiva | `Reservation.overlaps`, `Hotel.createReservation` | `ck_reservations_dates`, `ix_reservations_overlap` |
| RN-06 | Cancelar libera las fechas | `Reservation.cancel` + `ReservationStatus.blocksRoom` | `status = CANCELLED` |
| RN-07 | Disponibilidad excluye cruces y mantenimiento | `Hotel.getAvailableRooms` | Consulta de referencia |
| RN-08 | Check-in: reserva pendiente, dentro de su periodo, habitación disponible → Ocupada | `Reservation.checkIn` | — |
| RN-09 | Check-out exige la cuenta saldada | `Hotel.checkOut` | `payments`, `trg_payments_full_amount` |
| RN-10 | Después del check-out, habitación en Mantenimiento/Limpieza | `Reservation.checkOut` | `rooms.status` |
| RN-11 | Total = noches × tarifa + cargo extra Suite + servicios | `Invoice`, `Room.calculatePrice` | `ck_invoices_total` |
| RN-12 | Factura inmutable y única por estadía | `Invoice` (campos `final`), `Hotel.generateInvoice`, `Hotel.addService` | `uq_invoices_reservation`, `trg_invoices_no_update/delete`, `trg_services_not_invoiced` |
| RN-13 | No reservar en el pasado ni en mantenimiento | `Hotel.createReservation`, `Room.isBookable` | — |
| RN-14 | Cambio de tipo solo sin reservas pendientes o activas | `Hotel.changeRoomType` | — |
| RN-15 | `OCCUPIED` solo por check-in / check-out | `Hotel.changeRoomStatus` | — |

## 4. Requisitos no funcionales

| RNF | Cómo se cumple |
| :--- | :--- |
| RNF01 Mantenibilidad (POO/SOLID) | `System_hotel` solo lee y muestra; las reglas viven en `Hotel` y las entidades. Interfaces `IRoom` / `IGuest` (inversión de dependencias), `RoomFactory` (abierto/cerrado), subclases de `Room` (sustitución de Liskov). |
| RNF02 Identificadores en inglés | Todas las clases, métodos y atributos (RS-03). |
| RNF03 Menús numerados con "Volver" y confirmación | `System_hotel` (submenús con `0. Volver`), `ConsoleUtils.confirm`. |
| RNF04 Validación sin cierres abruptos | Validación en constructores y setters; `IllegalArgumentException`, `BusinessRuleException` (con código de regla) y `EntityNotFoundException` capturadas por el menú. Restricciones `CHECK`/`UNIQUE`/FK en la BD. |
| RNF05 Rendimiento | Búsquedas O(n) sobre listas en memoria; índices únicos y compuesto en la BD. |
| RNF06 Java 17 | `pom.xml`: `maven.compiler.release = 17`; solo librerías del JDK. |
| RNF07 Formatos | `ConsoleUtils.readDate` / `formatDate` (dd/mm/aaaa), `readMoney` / `formatCop` (COP), `BigDecimal`. |

## 5. Pendientes conocidos

1. Conectar la aplicación a la base de datos (riesgo R-01 del SRS).
2. Control de acceso real en la aplicación (riesgo R-02); el prototipo solo lo demuestra.
3. Registro de auditoría (riesgo R-03).
