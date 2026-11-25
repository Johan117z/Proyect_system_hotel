-- =====================================================================
-- system_hotel_db  v2.1  (MySQL 8.0.16+ por las restricciones CHECK)
-- Alineado con: HU-01..HU-14 (Fase 1), SRS v2.0 (RF-01..RF-14, RN-01..RN-15),
-- diagrama de clases y código Java (Fase 6).
-- =====================================================================
DROP DATABASE IF EXISTS system_hotel_db;
CREATE DATABASE system_hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE system_hotel_db;

-- 1. Huéspedes (HU-05..HU-08) -> clase Guest / Person
CREATE TABLE guests (
    id         SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    document   VARCHAR(10) NOT NULL,                       -- cédula colombiana: 6 a 10 dígitos
    full_name  VARCHAR(60) NOT NULL,
    email      VARCHAR(80) NOT NULL,
    phone      CHAR(10)    NOT NULL,                       -- celular colombiano: 10 dígitos
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,          -- HU-07: baja lógica, conserva el historial
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_guests_document UNIQUE (document),
    CONSTRAINT ck_guests_document CHECK (document REGEXP '^[0-9]{6,10}$'),
    CONSTRAINT ck_guests_phone    CHECK (phone REGEXP '^[0-9]{10}$'),
    CONSTRAINT ck_guests_email    CHECK (email LIKE '%_@_%._%')
);

-- 2. Habitaciones (HU-01..HU-04) -> IRoom / Room / SingleRoom / DoubleRoom / SuiteRoom
--    Herencia mapeada como tabla única (single table): `type` es el discriminador.
CREATE TABLE rooms (
    id                SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    room_number       SMALLINT UNSIGNED NOT NULL,
    type              ENUM('SINGLE', 'DOUBLE', 'SUITE') NOT NULL,
    capacity          TINYINT UNSIGNED NOT NULL,
    price_per_night   DECIMAL(10, 2) NOT NULL,
    extra_service_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- solo SUITE (SuiteRoom.extraServiceFee)
    -- MAINTENANCE = "Mantenimiento/Limpieza": estado de la habitación después del check-out (RN-10)
    status            ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,        -- HU-03: baja lógica, conserva el historial
    CONSTRAINT uq_rooms_number      UNIQUE (room_number),
    CONSTRAINT ck_rooms_number      CHECK (room_number > 0),
    CONSTRAINT ck_rooms_capacity    CHECK (capacity > 0),
    CONSTRAINT ck_rooms_price       CHECK (price_per_night > 0),
    CONSTRAINT ck_rooms_extra_fee   CHECK (extra_service_fee >= 0),
    CONSTRAINT ck_rooms_fee_suite   CHECK (type = 'SUITE' OR extra_service_fee = 0)
);

-- 3. Reservas (HU-09..HU-13) -> clase Reservation, enum ReservationStatus
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
    -- Acelera la consulta de cruce de fechas de RN-05 / RN-07
    INDEX ix_reservations_overlap (room_id, status, check_in_date, check_out_date)
);

-- 3b. Consecutivo del código RES-AAAA-NNN.
--     La API lo lee con SELECT ... FOR UPDATE al crear una reserva: el bloqueo de esta fila hace que
--     dos reservas simultáneas no reciban el mismo código y que el control de cruce (RN-05) no se salte.
CREATE TABLE code_sequences (
    name       VARCHAR(30) PRIMARY KEY,
    next_value INT UNSIGNED NOT NULL,
    CONSTRAINT ck_code_sequences_value CHECK (next_value > 0)
);

-- 4. Servicios adicionales consumidos durante la estadía (HU-14) -> clase AdditionalService
CREATE TABLE additional_services (
    id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT UNSIGNED NOT NULL,
    description    VARCHAR(80) NOT NULL,
    amount         DECIMAL(10, 2) NOT NULL,
    registered_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_services_amount CHECK (amount > 0),
    CONSTRAINT fk_services_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT
);

-- 5. Facturas (HU-14, RN-11, RN-12) -> clase Invoice
--    Se emite durante la estadía (reserva ACTIVE), antes del check-out.
--    Copia tarifa y cargos al emitirse: un cambio posterior de precio no altera la factura.
CREATE TABLE invoices (
    id                  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    reservation_id      INT UNSIGNED NOT NULL,
    stay_days           SMALLINT UNSIGNED NOT NULL,
    night_rate          DECIMAL(10, 2) NOT NULL,
    extra_service_fee   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,  -- cargo de la suite
    additional_services DECIMAL(10, 2) NOT NULL DEFAULT 0.00,  -- suma de additional_services de la reserva
    total_amount        DECIMAL(12, 2) NOT NULL,
    issue_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invoices_reservation UNIQUE (reservation_id),   -- 1 reserva : 1 factura
    CONSTRAINT ck_invoices_values CHECK (stay_days > 0 AND night_rate > 0
                                         AND extra_service_fee >= 0 AND additional_services >= 0),
    CONSTRAINT ck_invoices_total  CHECK (total_amount =
                                         stay_days * night_rate + extra_service_fee + additional_services),
    -- RN-12: RESTRICT. Borrar una reserva no puede borrar su factura.
    CONSTRAINT fk_invoices_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT
);

-- 6. Pagos (RN-09: el check-out exige la cuenta saldada) -> clase Payment, enum PaymentMethod
--    Tabla aparte para que la factura siga siendo inmutable (RN-12). Un pago total por factura.
CREATE TABLE payments (
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT UNSIGNED NOT NULL,
    amount     DECIMAL(12, 2) NOT NULL,
    method     ENUM('CASH', 'CARD', 'TRANSFER') NOT NULL,
    paid_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payments_invoice UNIQUE (invoice_id),
    CONSTRAINT ck_payments_amount  CHECK (amount > 0),
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE RESTRICT
);

DELIMITER $$
-- RN-12: las facturas son inmutables también a nivel de base de datos.
CREATE TRIGGER trg_invoices_no_update BEFORE UPDATE ON invoices
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'RN-12: una factura emitida no se puede modificar.';
END$$

CREATE TRIGGER trg_invoices_no_delete BEFORE DELETE ON invoices
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'RN-12: una factura emitida no se puede eliminar.';
END$$

-- RN-09: el pago debe saldar exactamente el total de la factura.
CREATE TRIGGER trg_payments_full_amount BEFORE INSERT ON payments
FOR EACH ROW
BEGIN
    IF NEW.amount <> (SELECT total_amount FROM invoices WHERE id = NEW.invoice_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'RN-09: el pago debe ser igual al total de la factura.';
    END IF;
END$$

-- No se agregan servicios a una estadía que ya fue facturada (Hotel.addService).
CREATE TRIGGER trg_services_not_invoiced BEFORE INSERT ON additional_services
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM invoices WHERE reservation_id = NEW.reservation_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'RN-12: la estadía ya fue facturada; no se agregan servicios.';
    END IF;
END$$
DELIMITER ;

-- =====================================================================
-- Datos de prueba (mismas habitaciones y huéspedes que System_hotel.loadSeedData)
-- =====================================================================
INSERT INTO guests (document, full_name, email, phone) VALUES
('1075234567', 'Carlos Pérez',         'carlos.perez@example.com', '3101234567'),
('1075987654', 'Ana María Gómez',      'ana.gomez@example.com',    '3209876543'),
('1076543210', 'Luis Fernando Torres', 'luis.torres@example.com',  '3154567890');

INSERT INTO rooms (room_number, type, capacity, price_per_night, extra_service_fee, status) VALUES
(101, 'SINGLE', 1, 120000.00,     0.00, 'AVAILABLE'),
(102, 'DOUBLE', 2, 200000.00,     0.00, 'AVAILABLE'),
(201, 'SUITE',  4, 350000.00, 50000.00, 'AVAILABLE'),   -- ya limpiada después del check-out de RES-2026-001
(202, 'SINGLE', 1, 120000.00,     0.00, 'MAINTENANCE');

-- Historial: una estadía completa (servicio, factura, pago y check-out) y una reserva futura pendiente.
-- Se insertan en el orden del flujo real para que los triggers lo permitan.
INSERT INTO reservations (reservation_code, guest_id, room_id, check_in_date, check_out_date, status) VALUES
('RES-2026-001', 1, 3, '2026-09-25', '2026-09-30', 'ACTIVE'),
('RES-2026-002', 2, 1, '2026-12-01', '2026-12-05', 'PENDING');

-- La próxima reserva creada por la API recibe el número 003
INSERT INTO code_sequences (name, next_value) VALUES ('reservation', 3);

INSERT INTO additional_services (reservation_id, description, amount) VALUES
(1, 'Lavandería', 30000.00);

-- 5 × 350.000 + 50.000 (suite) + 30.000 (servicios) = 1.830.000 COP
INSERT INTO invoices (reservation_id, stay_days, night_rate, extra_service_fee, additional_services, total_amount) VALUES
(1, 5, 350000.00, 50000.00, 30000.00, 1830000.00);

INSERT INTO payments (invoice_id, amount, method) VALUES
(1, 1830000.00, 'CARD');

UPDATE reservations SET status = 'COMPLETED' WHERE reservation_code = 'RES-2026-001';

-- =====================================================================
-- Consultas de referencia (las usará la capa de persistencia en una fase futura)
-- =====================================================================
-- RN-05 / RN-07: hay cruce si existe una reserva PENDING/ACTIVE de la habitación con
-- check_in_date < :nueva_salida AND check_out_date > :nueva_entrada.
-- SELECT r.* FROM rooms r
--  WHERE r.is_active AND r.status <> 'MAINTENANCE'
--    AND NOT EXISTS (SELECT 1 FROM reservations x
--                     WHERE x.room_id = r.id AND x.status IN ('PENDING', 'ACTIVE')
--                       AND x.check_in_date < :checkOut AND x.check_out_date > :checkIn);
--
-- RN-04: un huésped se puede eliminar (is_active = FALSE) si esta consulta devuelve 0.
-- SELECT COUNT(*) FROM reservations x
--   LEFT JOIN invoices i ON i.reservation_id = x.id
--   LEFT JOIN payments p ON p.invoice_id = i.id
--  WHERE x.guest_id = :guestId
--    AND (x.status IN ('PENDING', 'ACTIVE') OR (i.id IS NOT NULL AND p.id IS NULL));
