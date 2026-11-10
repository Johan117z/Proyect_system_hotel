# Software Architecture & UML Diagrams Specification

## Project: Hotel Management System (`System_hotel`)

This document provides the standard Object-Oriented Analysis and Design (OOAD) models for the Hotel Management System using UML 2.5 standards in PlantUML and Mermaid formats.

---

## 1. Use Case Diagram

Defines the system boundaries and interactions between actors (`Administrator` and `Receptionist`) and functional capabilities.

```mermaid
graph TD
    subgraph SystemBoundary["Hotel Management System (System_hotel)"]
        UC1["UC-01: Register Room"]
        UC2["UC-02: Update Room"]
        UC3["UC-03: Delete Room"]
        UC4["UC-04: Query Rooms"]

        UC5["UC-05: Register Guest"]
        UC6["UC-06: Query Guest"]

        UC7["UC-07: Create Reservation"]
        UC8["UC-08: Process Check-in"]
        UC9["UC-09: Process Check-out"]
        UC10["UC-10: Generate Invoice"]
        UC11["UC-11: Cancel Reservation"]
    end

    Admin["👤 Hotel Administrator"]
    Recep["👤 Receptionist"]

    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4

    Recep --> UC4
    Recep --> UC5
    Recep --> UC6
    Recep --> UC7
    Recep --> UC8
    Recep --> UC9
    Recep --> UC11

    UC9 -. "<<include>>" .-> UC10
    UC7 -. "<<include>>" .-> UC4
```

---

## 2. Class Diagram (Domain & Application Model)

Domain model v2.0, aligned with the SRS, the database schema (Phase 4) and the Java code (Phase 6). Money uses `BigDecimal` (DECIMAL in the database) and states use enums.

```plantuml
@startuml
skinparam classAttributeIconSize 0

enum RoomType {
    SINGLE
    DOUBLE
    SUITE
}

enum RoomStatus {
    AVAILABLE
    OCCUPIED
    MAINTENANCE
}

enum ReservationStatus {
    PENDING
    ACTIVE
    COMPLETED
    CANCELLED
    + blocksRoom(): boolean
}

interface IRoom {
    + getNumber(): int
    + getType(): RoomType
    + getCapacity(): int
    + getPricePerNight(): BigDecimal
    + getExtraServiceFee(): BigDecimal
    + getStatus(): RoomStatus
    + changeStatus(status: RoomStatus): void
    + setPricePerNight(price: BigDecimal): void
    + isBookable(): boolean
    + calculatePrice(nights: long): BigDecimal
}

abstract class Room {
    - number: int
    - capacity: int
    - pricePerNight: BigDecimal
    - status: RoomStatus
    + calculatePrice(nights: long): BigDecimal
}

class SingleRoom
class DoubleRoom
class SuiteRoom {
    - extraServiceFee: BigDecimal
    + getExtraServiceFee(): BigDecimal
}

class RoomFactory {
    + {static} create(type: RoomType, number: int, capacity: int, price: BigDecimal, fee: BigDecimal): Room
}

interface IGuest {
    + getDocument(): String
    + getFullName(): String
}

abstract class Person {
    - document: String
    - fullName: String
    - email: String
    - phone: String
}

class Guest {
    + updateContactInfo(email: String, phone: String): void
}

class Reservation {
    - reservationCode: String
    - checkInDate: LocalDate
    - checkOutDate: LocalDate
    - status: ReservationStatus
    + overlaps(start: LocalDate, end: LocalDate): boolean
    + getStayDays(): long
    + checkIn(): void
    + checkOut(): void
    + cancel(): void
}

class Invoice <<immutable>> {
    - invoiceId: int
    - stayDays: long
    - nightRate: BigDecimal
    - extraServiceFee: BigDecimal
    - additionalServices: BigDecimal
    - totalAmount: BigDecimal
    - issueDate: LocalDateTime
}

class Hotel {
    - rooms: List<IRoom>
    - guests: List<Guest>
    - reservations: List<Reservation>
    - invoices: List<Invoice>
    + registerRoom(room: IRoom): void
    + updateRoomRate(number: int, price: BigDecimal): void
    + changeRoomType(number: int, type: RoomType, ...): IRoom
    + changeRoomStatus(number: int, status: RoomStatus): void
    + deleteRoom(number: int): void
    + registerGuest(guest: Guest): void
    + updateGuestContact(document: String, email: String, phone: String): void
    + createReservation(document: String, roomNumber: int, checkIn: LocalDate, checkOut: LocalDate): Reservation
    + checkIn(code: String): Reservation
    + checkOut(code: String, additionalServices: BigDecimal): Invoice
    + cancelReservation(code: String): Reservation
}

class BusinessRuleException {
    - ruleCode: String
}
class EntityNotFoundException

IRoom <|.. Room
Room <|-- SingleRoom
Room <|-- DoubleRoom
Room <|-- SuiteRoom
RoomFactory ..> Room : creates
IGuest <|.. Guest
Person <|-- Guest

Hotel "1" o-- "0..*" IRoom : manages
Hotel "1" o-- "0..*" Guest : manages
Hotel "1" *-- "0..*" Reservation : manages
Hotel "1" *-- "0..*" Invoice : issues
Reservation "0..*" --> "1" IGuest : associated to
Reservation "0..*" --> "1" IRoom : assigned to
Invoice "0..1" --> "1" Reservation : generated from
Hotel ..> BusinessRuleException : throws
Hotel ..> EntityNotFoundException : throws
@enduml
```

---

## 3. Sequence Diagram (Reservation Creation Workflow)

Models message exchanges between domain components to satisfy business rules (overbooking check).

```plantuml
@startuml
autonumber
actor Receptionist as Recep
participant "System_hotel (CLI)" as CLI
participant "Hotel" as Hotel
participant "Reservation" as Res
participant "Room" as Room

Recep -> CLI : Inputs reservation details (doc, roomNum, dates)
CLI -> Hotel : createReservation(doc, roomNum, start, end)
activate Hotel

Hotel -> Hotel : findGuest(doc)
Hotel -> Hotel : findRoom(roomNum)

alt Guest or Room not found
    Hotel --> CLI : throws EntityNotFoundException
    CLI --> Recep : Displays "Entity not found"
else Entities exist
    loop For each existing reservation for target room
        Hotel -> Res : overlaps(start, end)
        Res --> Hotel : boolean
    end

    alt Date overlap detected (BR-01)
        Hotel --> CLI : throws BusinessRuleException("BR-01")
        CLI --> Recep : Displays "Room unavailable for dates"
    else Room available
        create Res
        Hotel -> Res : new Reservation(code, guest, room, start, end)
        Hotel -> Hotel : addReservation(res)
        Hotel --> CLI : returns Reservation (PENDING)
        CLI --> Recep : Displays "Reservation Created Successfully"
    end
end
deactivate Hotel
@enduml
```

---

## 4. Activity Diagram (Check-out & Billing Process)

Defines operational workflows, decision nodes, and system state transitions during guest check-out and invoice issue.

```plantuml
@startuml
start
:Receptionist initiates Check-out;
:Input Reservation ID;

if (Active reservation exists?) then (No)
    :Display error "No active reservation found";
    stop
else (Yes)
    :Enter additional services charge;
    :Calculate total stay duration (days);
    :Compute Total = (Stay Days * Night Rate) + Room Extra Fee + Additional Services;
    :Present billing draft to receptionist;

    if (Payment confirmed by guest?) then (No)
        :Cancel Check-out operation;
        stop
    else (Yes)
        :Generate Invoice record;
        :Set Reservation status to COMPLETED;
        :Update Room status to AVAILABLE;
        :Print final Invoice receipt;
        stop
    endif
endif
@enduml
```
