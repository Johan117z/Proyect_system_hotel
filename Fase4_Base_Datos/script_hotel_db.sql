-- =====================================================================
-- system_hotel_db  v2.0  (MySQL 8.0.16+ por las restricciones CHECK)
-- Alineado con: SRS (FR-01..FR-10, BR-01..BR-06), diagrama de clases y código Java (Fase 6).
-- =====================================================================
DROP DATABASE IF EXISTS system_hotel_db;
CREATE DATABASE system_hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE system_hotel_db;

-- 1. Huéspedes (FR-05, FR-06) -> clase Guest / Person
CREATE TABLE guests (
    id         SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    document   VARCHAR(10) NOT NULL,                       -- cédula colombiana: 6 a 10 dígitos
    full_name  VARCHAR(60) NOT NULL,
    email      VARCHAR(80) NOT NULL,
    phone      CHAR(10)    NOT NULL,                       -- celular colombiano: 10 dígitos
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_guests_document UNIQUE (document),
    CONSTRAINT ck_guests_document CHECK (document REGEXP '^[0-9]{6,10}$'),
    CONSTRAINT ck_guests_phone    CHECK (phone REGEXP '^[0-9]{10}$'),
    CONSTRAINT ck_guests_email    CHECK (email LIKE '%_@_%._%')
);

-- 2. Habitaciones (FR-01..FR-04) -> IRoom / Room / SingleRoom / DoubleRoom / SuiteRoom
--    Herencia mapeada como tabla única (single table): `type` es el discriminador.
CREATE TABLE rooms (
    id                SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    room_number       SMALLINT UNSIGNED NOT NULL,
    type              ENUM('SINGLE', 'DOUBLE', 'SUITE') NOT NULL,
    capacity          TINYINT UNSIGNED NOT NULL,
    price_per_night   DECIMAL(10, 2) NOT NULL,
    extra_service_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- solo SUITE (SuiteRoom.extraServiceFee)
    status            ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,        -- FR-04: baja lógica, conserva el historial
    CONSTRAINT uq_rooms_number      UNIQUE (room_number),
    CONSTRAINT ck_rooms_number      CHECK (room_number > 0),
    CONSTRAINT ck_rooms_capacity    CHECK (capacity > 0),
    CONSTRAINT ck_rooms_price       CHECK (price_per_night > 0),
    CONSTRAINT ck_rooms_extra_fee   CHECK (extra_service_fee >= 0),
    CONSTRAINT ck_rooms_fee_suite   CHECK (type = 'SUITE' OR extra_service_fee = 0)
);

-- 3. Reservas (FR-07..FR-10) -> clase Reservation, enum ReservationStatus
CREATE TABLE reservations (
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    reservation_code VARCHAR(15) NOT NULL,                  -- RES-AAAA-NNN
    guest_id         SMALLINT UNSIGNED NOT NULL,
    room_id          SMALLINT UNSIGNED NOT NULL,
    check_in_date    DATE NOT NULL,
    check_out_date   DATE NOT NULL,
    status           ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reservations_code UNIQUE (reservation_code),
    CONSTRAINT ck_reservations_code CHECK (reservation_code REGEXP '^RES-[0-9]{4}-[0-9]{3,}$'),
    CONSTRAINT ck_reservations_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT fk_reservations_guest FOREIGN KEY (guest_id) REFERENCES guests (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reservations_room  FOREIGN KEY (room_id)  REFERENCES rooms (id)  ON DELETE RESTRICT,
    -- Acelera la consulta de cruce de fechas de BR-01
    INDEX ix_reservations_overlap (room_id, status, check_in_date, check_out_date)
);

-- 4. Facturas (FR-09, BR-03) -> clase Invoice
--    Copia tarifa y cargos al emitirse: un cambio posterior de precio no altera la factura.
CREATE TABLE invoices (
    id                  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    reservation_id      INT UNSIGNED NOT NULL,
    stay_days           SMALLINT UNSIGNED NOT NULL,
    night_rate          DECIMAL(10, 2) NOT NULL,
    extra_service_fee   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,  -- cargo de la suite
    additional_services DECIMAL(10, 2) NOT NULL DEFAULT 0.00,  -- consumos registrados en el check-out
    total_amount        DECIMAL(12, 2) NOT NULL,
    issue_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invoices_reservation UNIQUE (reservation_id),   -- 1 reserva : 1 factura
    CONSTRAINT ck_invoices_values CHECK (stay_days > 0 AND night_rate > 0
                                         AND extra_service_fee >= 0 AND additional_services >= 0),
    CONSTRAINT ck_invoices_total  CHECK (total_amount =
                                         stay_days * night_rate + extra_service_fee + additional_services),
    -- BR-03: RESTRICT (antes CASCADE). Borrar una reserva no puede borrar su factura.
    CONSTRAINT fk_invoices_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT
);

-- BR-03: las facturas son inmutables también a nivel de base de datos.
DELIMITER $$
CREATE TRIGGER trg_invoices_no_update BEFORE UPDATE ON invoices
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'BR-03: una factura emitida no se puede modificar.';
END$$

CREATE TRIGGER trg_invoices_no_delete BEFORE DELETE ON invoices
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'BR-03: una factura emitida no se puede eliminar.';
END$$
DELIMITER ;

-- =====================================================================
-- Datos de prueba (los mismos que System_hotel.loadSeedData)
-- =====================================================================
INSERT INTO guests (document, full_name, email, phone) VALUES
('1075234567', 'Carlos Pérez',         'carlos.perez@example.com', '3101234567'),
('1075987654', 'Ana María Gómez',      'ana.gomez@example.com',    '3209876543'),
('1076543210', 'Luis Fernando Torres', 'luis.torres@example.com',  '3154567890');

INSERT INTO rooms (room_number, type, capacity, price_per_night, extra_service_fee, status) VALUES
(101, 'SINGLE', 1, 120000.00,     0.00, 'AVAILABLE'),
(102, 'DOUBLE', 2, 200000.00,     0.00, 'AVAILABLE'),
(201, 'SUITE',  4, 350000.00, 50000.00, 'AVAILABLE'),
(202, 'SINGLE', 1, 120000.00,     0.00, 'MAINTENANCE');

-- Historial: una estadía ya facturada y una reserva futura pendiente
INSERT INTO reservations (reservation_code, guest_id, room_id, check_in_date, check_out_date, status) VALUES
('RES-2026-001', 1, 3, '2026-09-25', '2026-09-30', 'COMPLETED'),
('RES-2026-002', 2, 1, '2026-12-01', '2026-12-05', 'PENDING');

-- 5 × 350.000 + 50.000 (suite) + 0 = 1.800.000 COP
INSERT INTO invoices (reservation_id, stay_days, night_rate, extra_service_fee, additional_services, total_amount) VALUES
(1, 5, 350000.00, 50000.00, 0.00, 1800000.00);

-- =====================================================================
-- Consulta de referencia para BR-01 (la usará ReservationRepository en Spring Boot).
-- Hay cruce si existe una reserva PENDING/ACTIVE de la habitación con
-- check_in_date < :nueva_salida AND check_out_date > :nueva_entrada.
-- =====================================================================
-- SELECT COUNT(*) FROM reservations
--  WHERE room_id = :roomId AND status IN ('PENDING', 'ACTIVE')
--    AND check_in_date < :checkOut AND check_out_date > :checkIn;
