# Phase 4: Relational Database Module (`system_hotel_db`) — v2.0

## 📌 Overview

This module contains the schema creation script and seed data for the **System Hotel** relational database. The structure follows Third Normal Form (3NF) and enforces referential integrity with primary keys, foreign keys and `CHECK` constraints.

Version 2.0 aligns the schema with the SRS, the class diagram and the Java domain model (Phase 6). See `docs/MATRIZ_TRAZABILIDAD.md` for the full requirement → class → table mapping.

## 🛠️ Execution Environment

- **Database Engine:** MySQL 8.0.16+ (required for `CHECK` constraints)
- **Container Environment:** Docker Desktop (`mysql-container`)
- **Port Mapping:** `3306:3306`
- **Database Client Tool:** MySQL Workbench

## 📐 Relational Schema & Data Dictionary

### 1. `guests` — class `Guest` (extends `Person`)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | SMALLINT UNSIGNED | PK, AUTO_INCREMENT | Internal identifier |
| `document` | VARCHAR(10) | UNIQUE, NOT NULL, CHECK 6–10 digits | Colombian ID number |
| `full_name` | VARCHAR(60) | NOT NULL | Guest full name |
| `email` | VARCHAR(80) | NOT NULL, CHECK format | Contact email |
| `phone` | CHAR(10) | NOT NULL, CHECK 10 digits | Mobile phone |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration date |

### 2. `rooms` — interface `IRoom`, classes `SingleRoom` / `DoubleRoom` / `SuiteRoom`

Inheritance is mapped as a **single table**: `type` acts as the discriminator column.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | SMALLINT UNSIGNED | PK, AUTO_INCREMENT | Internal identifier |
| `room_number` | SMALLINT UNSIGNED | UNIQUE, NOT NULL, > 0 | Room number |
| `type` | ENUM('SINGLE','DOUBLE','SUITE') | NOT NULL | Enum `RoomType` |
| `capacity` | TINYINT UNSIGNED | NOT NULL, > 0 | Maximum occupancy |
| `price_per_night` | DECIMAL(10,2) | NOT NULL, > 0 | Nightly rate (COP) |
| `extra_service_fee` | DECIMAL(10,2) | DEFAULT 0, ≥ 0, only SUITE | Fixed fee per stay |
| `status` | ENUM('AVAILABLE','OCCUPIED','MAINTENANCE') | DEFAULT 'AVAILABLE' | Enum `RoomStatus` |
| `is_active` | BOOLEAN | DEFAULT TRUE | FR-04 logical delete |

### 3. `reservations` — class `Reservation`, enum `ReservationStatus`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | Internal identifier |
| `reservation_code` | VARCHAR(15) | UNIQUE, NOT NULL, CHECK `RES-YYYY-NNN` | Public code |
| `guest_id` | SMALLINT UNSIGNED | FK → `guests.id`, RESTRICT | Guest |
| `room_id` | SMALLINT UNSIGNED | FK → `rooms.id`, RESTRICT | Room |
| `check_in_date` | DATE | NOT NULL | Arrival date |
| `check_out_date` | DATE | NOT NULL, > `check_in_date` | Departure date (exclusive) |
| `status` | ENUM('PENDING','ACTIVE','COMPLETED','CANCELLED') | DEFAULT 'PENDING' | Lifecycle state |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation date |

Index `ix_reservations_overlap (room_id, status, check_in_date, check_out_date)` supports the BR-01 overlap query.

### 4. `invoices` — class `Invoice`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT UNSIGNED | PK, AUTO_INCREMENT | Invoice number |
| `reservation_id` | INT UNSIGNED | UNIQUE, FK → `reservations.id`, RESTRICT | 1 reservation : 1 invoice |
| `stay_days` | SMALLINT UNSIGNED | NOT NULL, > 0 | Nights billed |
| `night_rate` | DECIMAL(10,2) | NOT NULL, > 0 | Rate copied at issue time |
| `extra_service_fee` | DECIMAL(10,2) | DEFAULT 0 | Suite fee copied at issue time |
| `additional_services` | DECIMAL(10,2) | DEFAULT 0 | Charges entered at check-out |
| `total_amount` | DECIMAL(12,2) | CHECK = days × rate + fee + services | Total (COP) |
| `issue_date` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Issue timestamp |

Triggers `trg_invoices_no_update` and `trg_invoices_no_delete` enforce **BR-03 (invoice immutability)**.

## 🔄 Changes from v1.0

| Change | Reason |
|---|---|
| `reservations.reservation_id` → `reservation_code` | Avoid confusion with FK `invoices.reservation_id` |
| `invoices` FK `ON DELETE CASCADE` → `RESTRICT` + triggers | BR-03: invoices are immutable |
| Added `invoices.extra_service_fee` | Separate suite fee from check-out charges |
| `CHECK (check_out_date > check_in_date)` and format checks | NFR-01 data integrity |
| Added `rooms.is_active` | FR-04 delete without losing reservation history |
| Seed reservation `RES-2026-001` → `COMPLETED` | An `ACTIVE` reservation cannot already have an invoice |

## 🚀 Deployment & Execution Instructions

### 1. Launch Container

Start the MySQL Docker container via terminal:

```bash
docker start mysql-container
```

### 2. Run the script

```bash
docker exec -i mysql-container mysql -uroot -p < script_hotel_db.sql
```
