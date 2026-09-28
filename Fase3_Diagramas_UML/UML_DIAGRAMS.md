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

Presents object structures, memory references, encapsulation rules, and relationships adhering to SOLID design principles.

```plantuml
@startuml
skinparam classAttributeIconSize 0

enum RoomStatus {
    AVAILABLE
    OCCUPIED
    MAINTENANCE
}

class Room {
    - number: String
    - type: String
    - capacity: int
    - pricePerNight: double
    - status: RoomStatus
    + Room(number: String, type: String, capacity: int, pricePerNight: double)
    + getNumber(): String
    + getType(): String
    + getPricePerNight(): double
    + getStatus(): RoomStatus
    + setStatus(status: RoomStatus): void
}

class Guest {
    - document: String
    - name: String
    - email: String
    - phone: String
    + Guest(document: String, name: String, email: String, phone: String)
    + getDocument(): String
    + getName(): String
    + updateContactInfo(email: String, phone: String): void
}

class Reservation {
    - reservationId: String
    - guest: Guest
    - room: Room
    - checkInDate: LocalDate
    - checkOutDate: LocalDate
    - isActive: boolean
    + Reservation(reservationId: String, guest: Guest, room: Room, checkInDate: LocalDate, checkOutDate: LocalDate)
    + getReservationId(): String
    + getRoom(): Room
    + getGuest(): Guest
    + cancel(): void
    + hasOverlap(startDate: LocalDate, endDate: LocalDate): boolean
}

class Invoice {
    - invoiceId: String
    - reservation: Reservation
    - stayDays: int
    - nightRate: double
    - additionalServices: double
    - totalAmount: double
    + Invoice(invoiceId: String, reservation: Reservation, additionalServices: double)
    + calculateTotal(): double
    + printDetails(): void
}

class Hotel {
    - rooms: List<Room>
    - guests: List<Guest>
    - reservations: List<Reservation>
    - invoices: List<Invoice>
    + registerRoom(room: Room): boolean
    + registerGuest(guest: Guest): boolean
    + createReservation(doc: String, roomNum: String, start: LocalDate, end: LocalDate): boolean
    + processCheckIn(reservationId: String): boolean
    + processCheckOut(reservationId: String, additionalServices: double): boolean
}

Hotel "1" *-- "0..*" Room : manages
Hotel "1" *-- "0..*" Guest : manages
Hotel "1" *-- "0..*" Reservation : manages
Hotel "1" *-- "0..*" Invoice : issues

Reservation "0..*" --> "1" Guest : associated to
Reservation "0..*" --> "1" Room : assigned to
Invoice "1" -- "1" Reservation : generated from
@enduml
```

---

## 3. Sequence Diagram (Reservation Creation Workflow)

Models message exchanges between domain components to satisfy business rules (overbooking check).

```plantuml
@startuml
autonumber
actor Receptionist as Recep
participant "ConsoleUtils" as CLI
participant "Hotel" as Hotel
participant "Reservation" as Res
participant "Room" as Room

Recep -> CLI : Inputs reservation details (doc, roomNum, dates)
CLI -> Hotel : createReservation(doc, roomNum, start, end)
activate Hotel

Hotel -> Hotel : findGuest(doc)
Hotel -> Hotel : findRoom(roomNum)

alt Guest or Room not found
    Hotel --> CLI : Returns false (Error)
    CLI --> Recep : Displays "Entity not found"
else Entities exist
    loop For each existing reservation for target room
        Hotel -> Res : hasOverlap(start, end)
        Res --> Hotel : boolean
    end

    alt Date overlap detected (BR-01)
        Hotel --> CLI : Returns false
        CLI --> Recep : Displays "Room unavailable for dates"
    else Room available
        create Res
        Hotel -> Res : new Reservation(guest, room, start, end)
        Hotel -> Hotel : addReservation(res)
        Hotel --> CLI : Returns true
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
    :Compute Total = (Stay Days * Night Rate) + Additional Services;
    :Present billing draft to receptionist;

    if (Payment confirmed by guest?) then (No)
        :Cancel Check-out operation;
        stop
    else (Yes)
        :Generate Invoice record;
        :Set Reservation status to COMPLETED;
        :Update Room status to AVAILABLE / MAINTENANCE;
        :Print final Invoice receipt;
        stop
    endif
endif
@enduml
```
