# Matriz de trazabilidad — System_hotel v2.0

Relaciona cada requisito del SRS con su historia de usuario, caso de uso, implementación Java y estructura en la base de datos. El objetivo es que los cuatro artefactos describan **el mismo sistema**.

## 1. Modelo canónico (decisiones de alineación)

| Concepto | Valor único en SRS, UML, BD y Java | Antes (inconsistente) |
|---|---|---|
| Estados de reserva | `PENDING → ACTIVE → COMPLETED`, `PENDING → CANCELLED` | Java usaba `CONFIRMED / CHECKED_IN / CHECKED_OUT`; el UML en PlantUML usaba `isActive: boolean` |
| Estados de habitación | `AVAILABLE / OCCUPIED / MAINTENANCE` (enum `RoomStatus`) | `String` libre en Java |
| Tipos de habitación | `SINGLE / DOUBLE / SUITE` → subclases de `Room` | `String` en Java; jerarquía solo en el PNG |
| Código de reserva | `RES-AAAA-NNN` (`reservation_code`) | `int` en Java; `reservation_id` en BD chocaba con la FK de `invoices` |
| Dinero | `BigDecimal` en Java, `DECIMAL` en BD, mostrado en COP | `double` en Java |
| Total de la factura | `noches × tarifa + cargo extra suite + servicios adicionales` | SRS: `noches × tarifa`; BD: con servicios; Java: sin cargo extra |
| Número de habitación | `int` / `SMALLINT` | `String` en el PNG del diagrama de clases |
| Documento del huésped | 6 a 10 dígitos | `CHAR(10)` fijo en BD, sin validar en Java |

## 2. Requisitos funcionales

| RF | HU | CU | Implementación Java | Base de datos | Verificado |
|---|---|---|---|---|---|
| FR-01 Registrar habitación | US-01 | UC-01 | `Hotel.registerRoom`, `RoomFactory.create`, validaciones en `Room` | `rooms`: `uq_rooms_number`, `ck_rooms_price`, `ck_rooms_capacity` | ✅ número duplicado rechazado |
| FR-02 Consultar habitaciones | — | UC-04 | `Hotel.getAllRooms`, `Hotel.getRoomsByStatus` | `rooms.status` | ✅ |
| FR-03 Actualizar habitación | — | UC-02 | `Hotel.updateRoomRate`, `Hotel.changeRoomType` (BR-05) | `rooms.price_per_night`, `rooms.type` | ✅ cambio de tipo bloqueado con reservas |
| FR-04 Eliminar habitación | — | UC-03 | `Hotel.deleteRoom` (baja lógica) | `rooms.is_active`; FK `RESTRICT` | ✅ bloqueado con reservas pendientes o activas |
| FR-05 Registrar huésped | US-02 | UC-05 | `Hotel.registerGuest`, validaciones en `Person` | `guests`: `uq_guests_document`, `ck_guests_*` | ✅ documento inválido rechazado |
| FR-06 Consultar/actualizar huésped | — | UC-06 | `Hotel.findGuest`, `Guest.updateContactInfo` | `guests.email`, `guests.phone` | ✅ |
| FR-07 Crear reserva | US-03 | UC-07 | `Hotel.createReservation` (BR-01, BR-04) | `reservations`, `ck_reservations_dates`, `ix_reservations_overlap` | ✅ nace en `PENDING` |
| FR-08 Check-in | US-04 | UC-08 | `Reservation.checkIn` (BR-02) | `reservations.status = ACTIVE`, `rooms.status = OCCUPIED` | ✅ doble check-in rechazado |
| FR-09 Check-out y factura | US-05 | UC-09, UC-10 | `Hotel.checkOut`, `Reservation.checkOut`, `Invoice` | `invoices`, `ck_invoices_total` | ✅ 5 × 350.000 + 50.000 + 30.000 = 1.830.000 |
| FR-10 Cancelar reserva | — | UC-11 | `Reservation.cancel` | `reservations.status = CANCELLED` | ✅ no cancela reservas activas |

## 3. Reglas de negocio

| Regla | Descripción | Java | BD | Estado en SRS |
|---|---|---|---|---|
| BR-01 | Sin sobreventa: no se cruzan fechas con reservas `PENDING`/`ACTIVE`. La salida es exclusiva (un huésped entra el día que otro sale) | `Reservation.overlaps`, `Hotel.createReservation` | Índice `ix_reservations_overlap` + consulta de referencia al final del script | Existente |
| BR-02 | El check-in exige una reserva `PENDING` y la habitación `AVAILABLE` | `Reservation.checkIn` | — | Existente (ampliada) |
| BR-03 | Factura inmutable | `Invoice` con campos `final`; copia tarifa y cargos | FK `RESTRICT` + triggers `trg_invoices_no_update/delete` | Existente |
| BR-04 | No se reserva en el pasado ni una habitación en `MAINTENANCE` | `Hotel.createReservation` | — | **Nueva, agregar al SRS** |
| BR-05 | El tipo de una habitación solo cambia si no tiene reservas `PENDING`/`ACTIVE` | `Hotel.changeRoomType` | — | **Nueva, agregar al SRS** |
| BR-06 | `OCCUPIED` solo se asigna con el check-in y se libera con el check-out | `Hotel.changeRoomStatus` | — | **Nueva, agregar al SRS** |

## 4. Requisitos no funcionales

| RNF | Cómo se cumple |
|---|---|
| NFR-01 Integridad y validación | Validación en constructores y setters de Java **y** restricciones `CHECK`/`UNIQUE`/FK en la BD. Errores tipificados: `IllegalArgumentException` (dato inválido → 400), `BusinessRuleException` (regla → 409), `EntityNotFoundException` (→ 404) |
| NFR-02 Mantenibilidad / SOLID | `System_hotel` solo lee y muestra; las reglas viven en `Hotel` y las entidades. `IRoom`/`IGuest` (inversión de dependencias), `RoomFactory` (abierto/cerrado), subclases de `Room` (sustitución de Liskov) |
| NFR-03 Rendimiento | Búsquedas O(n) sobre listas en memoria; en BD, índices únicos e índice compuesto para BR-01 |

## 5. Cambios pendientes en el SRS (para la v2.0)

1. **FR-09:** cambiar la fórmula a `noches × tarifa + cargo extra de la suite + servicios adicionales`.
2. **FR-04:** indicar que la eliminación es lógica (`is_active`), para conservar el historial de reservas y facturas.
3. **FR-03:** agregar la restricción de BR-05 para el cambio de tipo.
4. Agregar **BR-04, BR-05 y BR-06**.
5. Llevar al `SRS.md` las restricciones RS-01/RS-02 y el Anexo C, que hoy solo están en el PDF.
6. Corregir el README de Fase 5: menciona 14 RF (el SRS tiene 10) y archivos que no existen (`mockups.html`, `prototipo-funcional.html`).
7. Regenerar `diagrama_clases_hotel.png` desde el PlantUML actualizado de `UML_DIAGRAMS.md`: el PNG todavía usa `String` para el número de habitación y `double` para el dinero.
