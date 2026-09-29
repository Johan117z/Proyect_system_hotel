DROP DATABASE IF EXISTS system_hotel_db;
CREATE DATABASE system_hotel_db;
USE system_hotel_db;

-- 1. Tabla Guests
CREATE TABLE guests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    document VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

-- 2. Tabla Rooms
CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_number INT UNIQUE NOT NULL,
    type ENUM('SINGLE', 'DOUBLE', 'SUITE') NOT NULL,
    capacity INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE'
);

-- 3. Tabla Reservations
CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id VARCHAR(50) UNIQUE NOT NULL,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
);

-- 4. Tabla Invoices
CREATE TABLE invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE,
    stay_days INT NOT NULL,
    night_rate DECIMAL(10, 2) NOT NULL,
    additional_services DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

USE system_hotel_db;

-- 1. Insertar Huéspedes de prueba
INSERT INTO guests (document, full_name, email, phone) VALUES 
('1075234567', 'Carlos Pérez', 'carlos.perez@example.com', '3101234567'),
('1075987654', 'Ana María Gómez', 'ana.gomez@example.com', '3209876543'),
('1076543210', 'Luis Fernando Torres', 'luis.torres@example.com', '3154567890');

-- 2. Insertar Habitaciones con diferentes estados y tipos
INSERT INTO rooms (room_number, type, capacity, price_per_night, status) VALUES 
(101, 'SINGLE', 1, 120000.00, 'AVAILABLE'),
(102, 'DOUBLE', 2, 200000.00, 'AVAILABLE'),
(201, 'SUITE', 4, 350000.00, 'OCCUPIED'),
(202, 'SINGLE', 1, 120000.00, 'MAINTENANCE');

-- 3. Insertar Reservas (asociando IDs de huéspedes y habitaciones existentes)
INSERT INTO reservations (reservation_id, guest_id, room_id, check_in_date, check_out_date, status) VALUES 
('RES-2026-001', 1, 3, '2026-09-25', '2026-09-30', 'ACTIVE'),
('RES-2026-002', 2, 1, '2026-10-01', '2026-10-05', 'PENDING');

-- 4. Insertar Factura de prueba (asociada a la primera reserva)
INSERT INTO invoices (reservation_id, stay_days, night_rate, additional_services, total_amount) VALUES 
(1, 5, 350000.00, 50000.00, 1800000.00);


