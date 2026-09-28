# Software Requirements Specification (SRS)

## Project: Hotel Management System (System_hotel)

**Standard:** IEEE Std 830-1998  
**Version:** 1.0

---

## 1. Introduction

### 1.1 Purpose

The purpose of this document is to define the functional and non-functional requirements for the **Hotel Management System (`System_hotel`)**. This specification serves as the primary technical foundation for software architecture, object-oriented design, implementation, and system verification.

### 1.2 Scope

The `System_hotel` application is a core management system designed to streamline hotel administrative operations. The system handles:

- Inventory management of hotel rooms.
- Guest profile registrations.
- Reservation lifecycle management.
- Check-in and Check-out operational workflows.
- Billing and financial invoice generation upon check-out.

---

## 2. Overall Description

### 2.1 Product Perspective

The application is a standalone, object-oriented Java system operating via a Command Line Interface (CLI) or modular service layer, designed with extensible software architecture for future GUI or database integration.

### 2.2 User Classes and Characteristics

- **Hotel Administrator / Manager:** Manages room inventory (CRUD operations) and reviews overall operational reports.
- **Receptionist / Front Desk Staff:** Handles day-to-day guest registrations, reservation creation, check-in, check-out, and billing procedures.

### 2.3 Operating Environment

- **Platform:** Java Development Kit (JDK) 17 or higher.
- **Build System:** Apache Maven / Standard JDK.
- **Environment:** Cross-platform (Windows, macOS, Linux).

---

## 3. Specific Requirements

### 3.1 Functional Requirements (FR)

#### 🔹 Module 1: Room Inventory Management

- **FR-01: Register Room**
  - The system **shall** allow creating new room entities with attributes: `roomNumber` (Integer, Unique), `roomType` (Enum: SINGLE, DOUBLE, SUITE), `pricePerNight` (Double > 0), and initial `status` (`AVAILABLE`).
- **FR-02: Query Rooms**
  - The system **shall** provide queries to list all rooms, filter strictly by `AVAILABLE` status, or filter strictly by `OCCUPIED` status.
- **FR-03: Update Room Details**
  - The system **shall** allow modifying the room type and nightly rate for an existing room entity.
- **FR-04: Delete Room**
  - The system **shall** allow removing a room from inventory only if it has no active or pending reservations associated with it.

#### 🔹 Module 2: Guest Management

- **FR-05: Register Guest**
  - The system **shall** store guest profiles with attributes: `identificationNumber` (String, Unique), `fullName` (String), `email` (String), and `phoneNumber` (String).
- **FR-06: Query and Update Guest**
  - The system **shall** allow searching guest records by identification number and updating personal contact details.

#### 🔹 Module 3: Reservation & Operations Management

- **FR-07: Create Reservation**
  - The system **shall** create a reservation linking a valid `Guest` and an `AVAILABLE` `Room` for specified start and end dates.
  - The reservation status **shall** default to `PENDING`.
- **FR-08: Process Check-in**
  - The system **shall** transition a reservation status from `PENDING` to `ACTIVE` and automatically update the corresponding room status to `OCCUPIED`.
- **FR-09: Process Check-out & Invoicing**
  - The system **shall** transition an `ACTIVE` reservation to `COMPLETED`, calculate total lodging cost (`days * pricePerNight`), generate an `Invoice` record, and return the room status to `AVAILABLE`.
- **FR-10: Cancel Reservation**
  - The system **shall** allow cancelling a `PENDING` reservation, restoring any associated room locks to `AVAILABLE`.

---

### 3.2 Non-Functional Requirements (NFR)

- **NFR-01: Data Integrity & Validation**
  - The system **must** validate domain constraints prior to state mutations (e.g., negative prices, invalid date ranges, or duplicate IDs must trigger defensive exceptions).
- **NFR-02: Maintainability & Architecture**
  - Codebase structure **must** follow SOLID design principles, single responsibility principles, and maintain clean separation between domain models and user interfaces.
- **NFR-03: Performance**
  - Memory lookup operations for room and reservation status checks **must** execute in $O(1)$ or $O(n)$ time complexity within local collection structures.

---

## 4. Domain Business Rules (BR)

- **BR-01 (Overbooking Prevention):** A room cannot be reserved for date ranges that overlap with an existing active or pending reservation.
- **BR-02 (Mandatory Check-in Sequence):** Check-in operations require a valid, non-cancelled `PENDING` reservation ID.
- **BR-03 (Immutability of Invoices):** Once generated during Check-out, invoice monetary values and reference data cannot be altered.
