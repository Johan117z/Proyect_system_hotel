# Sistema Hotel – API REST (Spring Boot)

Esta es la migración del módulo de consola de la Fase 6 (`com.mycompany.system_hotel`) a una API REST por capas. Los paquetes siguen la misma estructura del ejercicio *supermarket*.

## Estructura de paquetes

```
src/main/java/com/mateojohan/sistemahotel
├── SistemahotelApplication.java
├── controllers/   → Reciben las peticiones HTTP y devuelven ApiResponse (sin try-catch)
├── services/      → Aplican las reglas de negocio (RN) con @Transactional
├── repositories/  → Interfaces JpaRepository y consultas JPQL / SQL nativo
├── models/        → Entidades JPA (herencia Room → Single/Double/Suite, Person → Guest)
├── enums/         → RoomType, RoomStatus, ReservationStatus, PaymentMethod
├── dto/           → DTO de entrada (@Valid) y de salida, y el wrapper ApiResponse<T>
├── exceptions/    → Excepciones de negocio y GlobalExceptionHandler (@RestControllerAdvice)
├── config/        → CorsConfig: orígenes que pueden consumir la API
└── utils/         → Formatos para los mensajes (dd/mm/aaaa y COP)
```

| Capa | Clases |
|---|---|
| controllers | `RoomController`, `GuestController`, `ReservationController`, `InvoiceController` |
| services | `RoomService`, `GuestService`, `ReservationService`, `BillingService` |
| repositories | `RoomRepository`, `GuestRepository`, `ReservationRepository`, `AdditionalServiceRepository`, `InvoiceRepository`, `PaymentRepository` |
| models | `Person`, `Guest`, `Room`, `SingleRoom`, `DoubleRoom`, `SuiteRoom`, `RoomFactory`, `Reservation`, `AdditionalService`, `Invoice`, `Payment` |

## Puesta en marcha

1. Crear la base de datos con `src/main/resources/db/script_hotel_db.sql`, que es una copia de `Fase4_Base_Datos/script_hotel_db.sql`. Ese script define las tablas, los CHECK, los triggers y los datos de prueba. Spring no lo ejecuta solo: se carga a mano.
   - CMD / Git Bash: `docker exec -i hotel-mysql mysql -uroot -proot < src/main/resources/db/script_hotel_db.sql`
   - PowerShell: `Get-Content src/main/resources/db/script_hotel_db.sql -Raw | docker exec -i hotel-mysql mysql -uroot -proot`
2. Ajustar `DB_URL`, `DB_USER` y `DB_PASSWORD`, o dejar los valores por defecto de `application.properties` (`localhost:3306`, `root/root`).
3. Ejecutar `./mvnw spring-boot:run`.
4. Abrir **http://localhost:8080/** en el navegador. Ahí se carga el frontend.

- `ddl-auto=none`: Hibernate no modifica el esquema; el script SQL es la fuente de verdad.
- El test `SistemahotelApplicationTests` levanta el contexto completo, así que necesita la base de datos activa.

## Frontend (integración)

`src/main/resources/static/index.html` es el prototipo de la Fase 5 ("Gran Continental") conectado a la API. Ya no usa `localStorage`: cada pantalla lee y escribe con `fetch` sobre los endpoints de abajo.

| Pantalla | Endpoints que usa |
|---|---|
| Panel general | `GET /api/rooms`, `/api/reservations`, `/api/invoices`, `/api/guests` |
| Habitaciones | `POST /api/rooms`, `PATCH .../rate`, `PUT .../type`, `PATCH .../status`, `DELETE` |
| Huéspedes | `POST`, `PUT`, `DELETE /api/guests`, `GET /api/guests/search?q=` |
| Reservas | `POST /api/reservations`, `PATCH .../cancel`, `PATCH .../check-in` |
| Disponibilidad | `GET /api/rooms/available` |
| Estadías | `POST .../services`, `POST .../invoice`, `POST /api/invoices/{id}/payment`, `PATCH .../check-out` |
| Facturación | `GET /api/invoices`, `GET /api/invoices/payments` |

- **Validación en dos capas:** el formulario valida antes de enviar y la API vuelve a validar. Los errores de la API se muestran en la ventana del formulario: el mensaje general con la regla (por ejemplo "(RN-05)") y cada campo marcado en rojo según `error.details`.
- **Login y roles:** el inicio de sesión con roles (Administrador / Recepcionista) funciona solo en la interfaz; la API aún no exige autenticación (riesgo R-02).
- **Abrirlo aparte:** también funciona abierto desde Live Server o `file://`. En ese caso llama a `http://localhost:8080` y necesita CORS (`config/CorsConfig`).

## Formato de respuesta (wrapper)

Todas las respuestas tienen la forma `{ success, data, error, meta }`.

| HTTP | `error.code` | Cuándo ocurre |
|---|---|---|
| 200 / 201 | – | Operación exitosa (201 en los POST que crean recursos) |
| 400 | `VALIDATION_ERROR`, `MALFORMED_REQUEST`, `INVALID_PARAMETER` | DTO inválido, JSON mal formado o fechas incoherentes |
| 404 | `RESOURCE_NOT_FOUND` | La habitación, el huésped, la reserva o la factura no existe |
| 409 | `BUSINESS_RULE_VIOLATION` | Se viola una regla de negocio; `error.details.rule` indica cuál (RN-xx) |
| 409 | `DATA_INTEGRITY_VIOLATION` | La BD rechazó la operación (UNIQUE, CHECK o trigger) |
| 500 | `INTERNAL_ERROR` | Error no controlado (no se expone el stack trace) |

## Endpoints

| Método | Ruta | HU / RN |
|---|---|---|
| GET | `/api/rooms?status=AVAILABLE` | HU-04 |
| GET | `/api/rooms/available?checkIn=2026-12-01&checkOut=2026-12-05` | HU-11 / RN-07 |
| GET | `/api/rooms/{roomNumber}` | HU-02 |
| POST | `/api/rooms` | HU-01 / RN-01 |
| PATCH | `/api/rooms/{roomNumber}/rate` | HU-02 / RN-12 |
| PUT | `/api/rooms/{roomNumber}/type` | HU-02 / RN-14 |
| PATCH | `/api/rooms/{roomNumber}/status` | RN-15 |
| DELETE | `/api/rooms/{roomNumber}` | HU-03 / RN-02 |
| GET | `/api/guests` | – |
| GET | `/api/guests/search?q=perez` | HU-08 |
| GET | `/api/guests/{document}` | HU-06 |
| POST | `/api/guests` | HU-05 / RN-03 |
| PUT | `/api/guests/{document}` | HU-06 |
| PATCH | `/api/guests/{document}/contact` | HU-06 |
| DELETE | `/api/guests/{document}` | HU-07 / RN-04 |
| GET | `/api/reservations?status=PENDING` | – |
| GET | `/api/reservations/{code}` | – |
| POST | `/api/reservations` | HU-09 / RN-05, RN-13 |
| PATCH | `/api/reservations/{code}/cancel` | HU-10 / RN-06 |
| PATCH | `/api/reservations/{code}/check-in` | HU-12 / RN-08 |
| POST | `/api/reservations/{code}/services` | HU-14 / RN-12 |
| POST | `/api/reservations/{code}/invoice` | HU-14 / RN-11, RN-12 |
| GET | `/api/reservations/{code}/invoice` | HU-14 |
| PATCH | `/api/reservations/{code}/check-out` | HU-13 / RN-09, RN-10 |
| GET | `/api/invoices` | – |
| GET | `/api/invoices/{id}` | – |
| POST | `/api/invoices/{id}/payment` | RN-09 |
| GET | `/api/invoices/payments` | – |

## Flujo completo de una estadía

1. `POST /api/reservations` crea la reserva → `PENDING`.
2. `PATCH /api/reservations/{code}/check-in` → la reserva pasa a `ACTIVE` y la habitación a `OCCUPIED`.
3. `POST /api/reservations/{code}/services` registra los servicios adicionales (opcional).
4. `POST /api/reservations/{code}/invoice` emite la factura, que queda inmutable.
5. `POST /api/invoices/{id}/payment` con `{ "method": "CARD" }` registra el pago total.
6. `PATCH /api/reservations/{code}/check-out` → la reserva pasa a `COMPLETED` y la habitación a `MAINTENANCE`.

## Ejemplos de cuerpo JSON

```json
// POST /api/rooms
{ "roomNumber": 301, "type": "SUITE", "capacity": 4, "pricePerNight": 350000, "extraServiceFee": 50000 }

// POST /api/guests
{ "document": "1075111222", "fullName": "María López", "email": "maria.lopez@example.com", "phone": "3001112233" }

// POST /api/reservations
{ "guestDocument": "1075111222", "roomNumber": 301, "checkInDate": "2026-12-10", "checkOutDate": "2026-12-12" }
```

## Pendiente

- Seguridad con Spring Security + JWT y control de acceso por rol (ADMIN / RECEPTIONIST) en la API, para que el RBAC no dependa solo de la interfaz.
- La interfaz no permite editar una reserva, porque la API no tiene ese endpoint: se cancela y se crea otra.
