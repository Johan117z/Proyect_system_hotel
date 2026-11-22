# Phase 6: Functional Development (`Sistema_hotel`)

## 📌 Overview

Java console application that implements the 14 user stories of Phase 1 (RF-01 to RF-14 of the SRS v2.0). It follows the class diagram of Phase 3 (interfaces, abstract classes and inheritance) and uses the same rules, limits and seed data as the database of Phase 4.

Data is kept in memory while the program runs (restriction RS-02). The database is designed and tested in Phase 4 but not connected yet.

## 🛠️ Requirements & Execution

- **JDK:** 17 or newer
- **Build tool:** Maven (project opened from NetBeans)
- **Main class:** `com.mycompany.system_hotel.System_hotel`

**From NetBeans:** open `Sistema_hotel` and press **Run Project (F6)**.

**From a terminal**, inside `Sistema_hotel`:

```bash
mvn compile exec:java
```

Without Maven:

```bash
javac -encoding UTF-8 -d target/classes $(find src -name "*.java")
java -cp target/classes com.mycompany.system_hotel.System_hotel
```

## 🧭 Menu

| Main menu | Options | User stories |
| :--- | :--- | :--- |
| 1. Habitaciones | Register, list (all / available / occupied / maintenance), change rate, change type and capacity, change status, delete | HU-01 to HU-04 |
| 2. Huéspedes | Register, search by name / last name / document, update, delete, list | HU-05 to HU-08 |
| 3. Reservas y disponibilidad | Create, availability by dates, list, cancel | HU-09 to HU-11 |
| 4. Estadía | Check-in, register additional service, view account, check-out | HU-12, HU-13 |
| 5. Facturación y pagos | Generate invoice, register payment, list invoices | HU-14 |
| 6. Reporte general | Rooms by status, guests, reservations, billed and collected | — |

Every submenu has `0. Volver`; deleting and cancelling ask for confirmation (RNF03).

## 🔁 Stay flow

1. **Check-in** of a pending reservation, from its check-in date until the night before check-out → reservation `ACTIVE`, room `OCCUPIED`.
2. **Additional services** registered during the stay.
3. **Invoice**: reservation nights × rate + Suite extra fee + services. One invoice per stay; it cannot be changed.
4. **Payment** of the full invoice (Efectivo, Tarjeta or Transferencia) → account settled.
5. **Check-out** (only with the account settled) → reservation `COMPLETED`, room `MAINTENANCE` (Mantenimiento/Limpieza).
6. After cleaning, the receptionist sets the room back to `AVAILABLE`.

## 📐 Class Structure

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| `IRoom` | Interface | Contract for every room type |
| `Room` | Abstract class | Number, capacity, rate, status and price calculation |
| `SingleRoom` / `DoubleRoom` / `SuiteRoom` | Classes | Room types; `SuiteRoom` adds a fixed extra fee per stay |
| `RoomFactory` | Factory | Creates the right subclass from a `RoomType` |
| `IGuest` | Interface | Contract the hotel uses to work with guests |
| `Person` / `Guest` | Abstract class / class | Personal data and its validation |
| `Reservation` | Class | Reservation life cycle, date overlap and services of the stay |
| `AdditionalService` | Immutable class | Service consumed during the stay |
| `Invoice` | Immutable class | Bill of the stay (RN-11, RN-12) |
| `Payment` | Immutable class | Full payment of an invoice (RN-09) |
| `Hotel` | Class | Business rules of the 14 user stories |
| `RoomType` / `RoomStatus` / `ReservationStatus` / `PaymentMethod` | Enums | Same values as the database ENUM columns |
| `BusinessRuleException` / `EntityNotFoundException` | Exceptions | Rule violation (with its rule code) / missing entity |
| `ConsoleUtils` | Utility | Validated input (numbers, COP amounts, dd/mm/aaaa dates, confirmation) and formatting |
| `System_hotel` | Main | Console menu; reads input and shows results |

## 🧪 Seed Data

Loaded at startup, same as `Fase4_Base_Datos/script_hotel_db.sql`:

- **Rooms:** 101 SINGLE $120.000 · 102 DOUBLE $200.000 · 201 SUITE $350.000 + $50.000 extra fee · 202 SINGLE $120.000 in `MAINTENANCE`.
- **Guests:** Carlos Pérez (1075234567), Ana María Gómez (1075987654), Luis Fernando Torres (1076543210).

Example: a 3-night stay in room 101 with a Minibar service of $25.000 produces an invoice of 3 × $120.000 + $25.000 = **$ 385.000 COP**.
