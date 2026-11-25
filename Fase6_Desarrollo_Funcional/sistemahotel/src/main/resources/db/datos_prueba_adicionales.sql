-- =====================================================================
-- system_hotel_db: datos de prueba adicionales para la demo y Postman
-- Ejecutar UNA sola vez, justo DESPUÉS de cargar script_hotel_db.sql en limpio (los ids 1-3 de huéspedes,
-- 1-4 de habitaciones y 1-2 de reservas los crea ese script). Si se corre dos veces falla por los UNIQUE.
-- Para volver a empezar: ejecutar de nuevo script_hotel_db.sql y luego este archivo.
--
-- Las estadías en curso y las reservas próximas usan CURDATE(): la demo funciona el día que se cargue.
-- Los códigos siguen la secuencia RES-AAAA-001..010; la API genera el siguiente (011).
-- Orden: reservas -> servicios -> facturas -> pagos, para respetar los triggers RN-09 y RN-12.
-- =====================================================================
USE system_hotel_db;

-- ---------------------------------------------------------------------
-- 1. Huéspedes (ids 4 a 10)
-- ---------------------------------------------------------------------
INSERT INTO guests (document, full_name, email, phone) VALUES
('1077123456', 'Valentina Rojas Medina',  'valentina.rojas@example.com',  '3112345678'),  -- 4: estadía completada con servicios
('1075456789', 'Santiago Cárdenas Ruiz',  'santiago.cardenas@example.com','3123456789'),  -- 5: estadía completada sin servicios
('52987654',   'María Fernanda Ospina',   'maria.ospina@example.com',     '3134567890'),  -- 6: reserva cancelada
('1076789012', 'Andrés Felipe Muñoz',     'andres.munoz@example.com',     '3145678901'),  -- 7: estadía activa con factura SIN pagar
('1078901234', 'Laura Camila Vargas',     'laura.vargas@example.com',     '3156789012'),  -- 8: reserva para HOY (probar check-in)
('80123456',   'Jorge Enrique Castaño',   'jorge.castano@example.com',    '3167890123'),  -- 9: reserva futura en suite
('1079012345', 'Daniela Sofía Herrera',   'daniela.herrera@example.com',  '3178901234');  -- 10: reserva futura

-- ---------------------------------------------------------------------
-- 2. Habitaciones (ids 5 a 11)
-- ---------------------------------------------------------------------
INSERT INTO rooms (room_number, type, capacity, price_per_night, extra_service_fee, status) VALUES
(103, 'SINGLE', 1, 120000.00,     0.00, 'AVAILABLE'),    -- 5
(104, 'DOUBLE', 2, 210000.00,     0.00, 'AVAILABLE'),    -- 6: reserva para hoy -> check-in
(203, 'DOUBLE', 3, 230000.00,     0.00, 'OCCUPIED'),     -- 7: estadía activa (reserva 7)
(204, 'SUITE',  4, 380000.00, 60000.00, 'AVAILABLE'),    -- 8: reserva futura -> RN-02 y RN-14
(301, 'SUITE',  5, 420000.00, 70000.00, 'OCCUPIED'),     -- 9: estadía activa (reserva 6)
(302, 'DOUBLE', 2, 200000.00,     0.00, 'AVAILABLE'),    -- 10
(303, 'SINGLE', 1, 115000.00,     0.00, 'MAINTENANCE');  -- 11: en limpieza -> no se puede reservar (RN-13)

-- ---------------------------------------------------------------------
-- 3. Reservas (ids 3 a 10). Cada una cubre un caso de prueba.
-- ---------------------------------------------------------------------
INSERT INTO reservations (reservation_code, guest_id, room_id, check_in_date, check_out_date, status) VALUES
-- 3: estadía terminada, con servicios, pagada por transferencia
('RES-2026-003',  4,  2, '2026-10-10', '2026-10-13', 'COMPLETED'),
-- 4: estadía terminada, sin servicios, pagada en efectivo
('RES-2026-004',  5,  5, '2026-11-02', '2026-11-04', 'COMPLETED'),
-- 5: reserva cancelada (sus fechas quedan libres, RN-06)
('RES-2026-005',  6,  6, '2026-12-20', '2026-12-24', 'CANCELLED'),
-- 6: estadía en curso, con servicios y SIN factura -> emitir factura, pagar y hacer check-out
('RES-2026-006',  3,  9, CURDATE() - INTERVAL 2 DAY, CURDATE() + INTERVAL 3 DAY, 'ACTIVE'),
-- 7: estadía en curso, facturada pero SIN pagar -> el check-out falla (RN-09) y el huésped no se puede eliminar (RN-04)
('RES-2026-007',  7,  7, CURDATE() - INTERVAL 1 DAY, CURDATE() + INTERVAL 2 DAY, 'ACTIVE'),
-- 8: reserva pendiente que empieza HOY -> check-in posible (RN-08)
('RES-2026-008',  8,  6, CURDATE(),                  CURDATE() + INTERVAL 2 DAY, 'PENDING'),
-- 9: reserva futura en suite -> no se puede eliminar la habitación (RN-02) ni cambiar su tipo (RN-14)
('RES-2026-009',  9,  8, CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 14 DAY, 'PENDING'),
-- 10: reserva futura -> sirve para probar el cruce de fechas (RN-05) y la cancelación
('RES-2026-010', 10, 10, CURDATE() + INTERVAL 20 DAY, CURDATE() + INTERVAL 23 DAY, 'PENDING');

-- El consecutivo de códigos continúa después de las reservas insertadas aquí (la API generará el 011)
UPDATE code_sequences SET next_value = (SELECT COUNT(*) + 1 FROM reservations) WHERE name = 'reservation';

-- ---------------------------------------------------------------------
-- 4. Servicios adicionales (antes de facturar: trigger trg_services_not_invoiced)
-- ---------------------------------------------------------------------
INSERT INTO additional_services (reservation_id, description, amount, registered_at) VALUES
(3, 'Restaurante',  85000.00, '2026-10-11 20:15:00'),
(3, 'Lavandería',   30000.00, '2026-10-12 09:40:00'),
(6, 'Desayuno',     45000.00, NOW() - INTERVAL 1 DAY),
(6, 'Spa',         120000.00, NOW()),
(7, 'Minibar',      28000.00, NOW()),
(7, 'Desayuno',     45000.00, NOW());

-- ---------------------------------------------------------------------
-- 5. Facturas (total = noches × tarifa + cargo suite + servicios; CHECK ck_invoices_total)
-- ---------------------------------------------------------------------
INSERT INTO invoices (reservation_id, stay_days, night_rate, extra_service_fee, additional_services, total_amount, issue_date) VALUES
-- Reserva 3: 3 × 200.000 + 0 + 115.000 = 715.000
(3, 3, 200000.00, 0.00, 115000.00, 715000.00, '2026-10-13 10:30:00'),
-- Reserva 4: 2 × 120.000 + 0 + 0 = 240.000
(4, 2, 120000.00, 0.00,      0.00, 240000.00, '2026-11-04 11:00:00'),
-- Reserva 7: 3 × 230.000 + 0 + 73.000 = 763.000 (queda SIN pago)
(7, 3, 230000.00, 0.00,  73000.00, 763000.00, NOW());

-- ---------------------------------------------------------------------
-- 6. Pagos (el monto debe ser igual al total de la factura: trigger RN-09)
-- ---------------------------------------------------------------------
INSERT INTO payments (invoice_id, amount, method, paid_at) VALUES
((SELECT id FROM invoices WHERE reservation_id = 3), 715000.00, 'TRANSFER', '2026-10-13 10:35:00'),
((SELECT id FROM invoices WHERE reservation_id = 4), 240000.00, 'CASH',     '2026-11-04 11:05:00');

-- ---------------------------------------------------------------------
-- Verificación rápida
-- ---------------------------------------------------------------------
SELECT r.reservation_code, g.full_name, rm.room_number, r.check_in_date, r.check_out_date, r.status,
       i.total_amount, IF(p.id IS NULL, IF(i.id IS NULL, 'Sin factura', 'Pendiente'), 'Pagada') AS cuenta
  FROM reservations r
  JOIN guests g  ON g.id = r.guest_id
  JOIN rooms  rm ON rm.id = r.room_id
  LEFT JOIN invoices i ON i.reservation_id = r.id
  LEFT JOIN payments p ON p.invoice_id = i.id
 ORDER BY r.id;
