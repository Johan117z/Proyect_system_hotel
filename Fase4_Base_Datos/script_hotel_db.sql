DROP DATABASE IF EXISTS system_hotel_db;
CREATE DATABASE system_hotel_db;
USE system_hotel_db;

-- 1. Tabla Guests
CREATE TABLE guests (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,   -- 2 bytes, hasta 65,535 huéspedes
    document CHAR(10) UNIQUE NOT NULL,                 -- 10 bytes fijos: cédula colombiana
    full_name VARCHAR(60) NOT NULL,                    -- nombre completo rara vez pasa de 60
    email VARCHAR(80) NOT NULL,                        -- 80 cubre casi cualquier email real
    phone CHAR(10) NOT NULL                            -- 10 bytes fijos: celular colombiano
);

-- 2. Tabla Rooms
CREATE TABLE rooms (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,   -- 2 bytes
    room_number SMALLINT UNSIGNED UNIQUE NOT NULL,     -- 2 bytes, hasta 65,535 habitaciones
    type ENUM('SINGLE', 'DOUBLE', 'SUITE') NOT NULL,   -- ENUM = 1 byte internamente
    capacity TINYINT UNSIGNED NOT NULL,                -- 1 byte, hasta 255 huéspedes/hab
    price_per_night DECIMAL(8, 2) UNSIGNED NOT NULL,   -- 4 bytes, hasta 999,999.99
    extra_service_fee DECIMAL(8, 2) UNSIGNED DEFAULT 0.00,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE'  -- 1 byte
);

-- 3. Tabla Reservations
CREATE TABLE reservations (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,        -- crece sin tope realista, se deja INT (4 bytes)
    reservation_id CHAR(13) UNIQUE NOT NULL,           -- 13 bytes fijos: RES-2026-001
    guest_id SMALLINT UNSIGNED NOT NULL,                -- 2 bytes
    room_id SMALLINT UNSIGNED NOT NULL,                 -- 2 bytes
    check_in_date DATE NOT NULL,                        -- 3 bytes
    check_out_date DATE NOT NULL,                       -- 3 bytes
    status ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',  -- 1 byte
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
);

-- 4. Tabla Invoices
CREATE TABLE invoices (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,         -- 4 bytes
    reservation_id INT UNSIGNED NOT NULL UNIQUE,        -- 4 bytes, debe matchear reservations.id
    stay_days TINYINT UNSIGNED NOT NULL,                -- 1 byte, hasta 255 noches (sobra)
    night_rate DECIMAL(8, 2) UNSIGNED NOT NULL,         -- 4 bytes
    additional_services DECIMAL(8, 2) UNSIGNED DEFAULT 0.00,
    total_amount DECIMAL(10, 2) UNSIGNED NOT NULL,      -- 5 bytes, suma puede superar 999,999.99
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- 4 bytes
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

USE system_hotel_db;

-- 1. Insertar Huéspedes de prueba
INSERT INTO guests (document, full_name, email, phone) VALUES 
('1075234567', 'Carlos Pérez', 'carlos.perez@example.com', '3101234567'),
('1075987654', 'Ana María Gómez', 'ana.gomez@example.com', '3209876543'),
('1076543210', 'Luis Fernando Torres', 'luis.torres@example.com', '3154567890');

-- 2. Insertar Habitaciones con diferentes estados y tipos (Incluyendo la tarifa extra para la Suite)
INSERT INTO rooms (room_number, type, capacity, price_per_night, extra_service_fee, status) VALUES 
(101, 'SINGLE', 1, 120000.00, 0.00, 'AVAILABLE'),
(102, 'DOUBLE', 2, 200000.00, 0.00, 'AVAILABLE'),
(201, 'SUITE', 4, 350000.00, 50000.00, 'OCCUPIED'),
(202, 'SINGLE', 1, 120000.00, 0.00, 'MAINTENANCE');

-- 3. Insertar Reservas (asociando IDs de huéspedes y habitaciones existentes)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, status) VALUES 
('RES-2026-001', 1, 3, '2026-09-25', '2026-09-30', 'ACTIVE'),
('RES-2026-002', 2, 1, '2026-10-01', '2026-10-05', 'PENDING');

-- 4. Insertar Factura de prueba (asociada a la primera reserva)
INSERT INTO invoices (reservation_id, stay_days, night_rate, additional_services, total_amount) VALUES 
(1, 5, 350000.00, 50000.00, 1800000.00);
