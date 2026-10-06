# Phase 4: Relational Database Module (`system_hotel_db`)

## 📌 Overview

This module contains the design, schema creation script, and initial seed data for the relational database of the **System Hotel** management system. The database structure adheres to the Third Normal Form (3NF) and enforces strict referential integrity using primary and foreign key constraints.

## 🛠️ Execution Environment

- **Database Engine:** MySQL 8.0+
- **Container Environment:** Docker Desktop (`mysql-container`)
- **Port Mapping:** `3306:3306`
- **Database Client Tool:** MySQL Workbench

## 📐 Relational Schema & Data Dictionary

### 1. `guests` Table

Stores personal information for guests registered in the system.

- `id` (INT, PK, AUTO_INCREMENT): Unique internal identifier.
- `document` (VARCHAR(20), UNIQUE, NOT NULL): National identity document or passport number.
- `full_name` (VARCHAR(100), NOT NULL): Guest's full name.
- `email` (VARCHAR(100), NOT NULL): Contact email address.
- `phone` (VARCHAR(20), NOT NULL): Contact phone number.

### 2. `rooms` Table

Manages physical room inventory and operational attributes.

- `id` (INT, PK, AUTO_INCREMENT): Unique internal identifier.
- `room_number` (INT, UNIQUE, NOT NULL): Room designation number.
- `type` (ENUM['SINGLE', 'DOUBLE', 'SUITE'], NOT NULL): Room classification type.
- `capacity` (INT, NOT NULL): Maximum occupancy capacity.
- `price_per_night` (DECIMAL(10,2), NOT NULL): Daily rate per night.
- `extra_service_fee` (DECIMAL(10,2), DEFAULT 0.00): Additional base fee applied exclusively to Suite rooms.
- `status` (ENUM['AVAILABLE', 'OCCUPIED', 'MAINTENANCE'], DEFAULT 'AVAILABLE'): Current operational status.

### 3. `reservations` Table

Handles room reservation lifecycles, establishing relationships between guests and rooms.

- `id` (INT, PK, AUTO_INCREMENT): Unique internal identifier.
- `reservation_id` (VARCHAR(50), UNIQUE, NOT NULL): Public reservation code or UUID.
- `guest_id` (INT, FK -> `guests.id`, ON DELETE RESTRICT): Foreign key referencing the guest.
- `room_id` (INT, FK -> `rooms.id`, ON DELETE RESTRICT): Foreign key referencing the room.
- `check_in_date` (DATE, NOT NULL): Scheduled check-in date.
- `check_out_date` (DATE, NOT NULL): Scheduled check-out date.
- `status` (ENUM['PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED'], DEFAULT 'PENDING'): Current reservation state.

### 4. `invoices` Table

Records billing details generated upon reservation completion or check-out.

- `id` (INT, PK, AUTO_INCREMENT): Unique internal identifier.
- `reservation_id` (INT, UNIQUE, FK -> `reservations.id`, ON DELETE CASCADE): Foreign key referencing the reservation.
- `stay_days` (INT, NOT NULL): Total number of nights stayed.
- `night_rate` (DECIMAL(10,2), NOT NULL): Room rate per night applied to the stay.
- `additional_services` (DECIMAL(10,2), DEFAULT 0.00): Ancillary charges (room service, amenities, etc.).
- `total_amount` (DECIMAL(10,2), NOT NULL): Total billing amount calculated.
- `issue_date` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP): Timestamp of invoice generation.

## 🚀 Deployment & Execution Instructions

### 1. Launch Container

Start the MySQL Docker container via terminal:

```bash
docker start mysql-container
```
