# User Stories Specification - Hotel Management System

This document contains the definition of Epics and User Stories for the development of the Hotel Management System, including Acceptance Criteria in BDD (Behavior-Driven Development) format.

---

## 📌 Epic 1: Inventory and Customer Management

### 🔹 US-01: Register New Room

**As a** Hotel Administrator  
**I want to** register a new room specifying its number, type, and price per night  
**So that** I can keep the inventory of available rooms updated in the system.

- **Acceptance Criteria:**
  - **Scenario 1 (Successful registration):**
    - **Given** that the administrator enters a room number that does not exist in the system.
    - **When** they complete the room type (Single, Double, Suite) and a price greater than 0.
    - **Then** the room is saved with the initial status `AVAILABLE`.
  - **Scenario 2 (Duplicate number):**
    - **Given** that a room with the entered number already exists.
    - **When** the administrator attempts to save the room.
    - **Then** the system rejects the operation and displays an error message ("Room number already exists").

---

### 🔹 US-02: Register Guest

**As a** Hotel Receptionist  
**I want to** register a customer's personal details (ID Document, Name, Email, Phone)  
**So that** I can associate them with future reservations and invoices.

- **Acceptance Criteria:**
  - **Scenario 1 (Successful registration):**
    - **Given** that the ID document number does not exist in the database.
    - **When** all required valid fields are entered.
    - **Then** the guest is correctly registered.
  - **Scenario 2 (Duplicate document):**
    - **Given** that the ID document is already registered.
    - **When** an attempt is made to register it again.
    - **Then** the system prevents duplicate registration and displays a message stating that the customer already exists.

---

## 📌 Epic 2: Reservation and Operations Management

### 🔹 US-03: Create Room Reservation

**As a** Hotel Receptionist  
**I want to** assign an available room to a guest for a specific date range  
**So that** I can guarantee the customer's stay without generating overbooking.

- **Acceptance Criteria:**
  - **Scenario 1 (Successful reservation):**
    - **Given** that the guest is registered and the selected room is in `AVAILABLE` status.
    - **When** a valid start and end date is selected (end date > start date).
    - **Then** the system generates a new Reservation in `PENDING` status and associates the reservation ID with the room.
  - **Scenario 2 (Room unavailable):**
    - **Given** that the selected room is in `OCCUPIED` or `MAINTENANCE` status.
    - **When** an attempt is made to create the reservation.
    - **Then** the system rejects the operation stating that the room is not available.

---

### 🔹 US-04: Perform Check-in

**As a** Hotel Receptionist  
**I want to** confirm the guest's formal check-in using their Reservation ID  
**So that** I can mark the room as occupied and start the stay period.

- **Acceptance Criteria:**
  - **Scenario 1 (Valid check-in):**
    - **Given** that a reservation exists in `PENDING` status for today.
    - **When** the receptionist processes the Check-in with the reservation ID.
    - **Then** the reservation status changes to `ACTIVE` and the associated room status changes to `OCCUPIED`.

---

### 🔹 US-05: Perform Check-out and Generate Invoice

**As a** Hotel Receptionist  
**I want to** finalize the guest's stay and calculate the total amount to pay  
**So that** I can release the room and issue the payment receipt.

- **Acceptance Criteria:**
  - **Scenario 1 (Correct Check-out and Billing):**
    - **Given** that the reservation is in `ACTIVE` status (`OCCUPIED`).
    - **When** the Check-out action is executed.
    - **Then** the system calculates the total cost by multiplying `days of stay * price per night`, the reservation status changes to `COMPLETED`, the room becomes `AVAILABLE` again, and an `Invoice` is generated with the total amount.
