# Diagramas UML — Sistema de Gestión Hotelera (`System_hotel`)

Versión 2.1, alineada con las 14 historias de usuario (Fase 1), el SRS v2.0 (Fase 2), la base de datos `system_hotel_db` (Fase 4) y el código Java (Fase 6).

Los diagramas están en **Mermaid**, que GitHub muestra directamente. Las imágenes de `Diagrams/` se generan a partir de este mismo código.

| Diagrama | Imagen |
| :--- | :--- |
| 1. Casos de uso | `Diagrams/diagrama_casos_de_uso.png` |
| 2. Clases | `Diagrams/diagrama_clases_hotel.png` |
| 3. Secuencia (crear reserva) | `Diagrams/diagrama_secuencia.png` |
| 4. Actividades (estadía: servicios, factura, pago y check-out) | `Diagrams/diagrama_actividades.png` |

---

## 1. Diagrama de casos de uso

Un único actor opera el sistema: el **Recepcionista** (SRS 2.3). El huésped es sujeto de los datos y no interactúa con el sistema. Cada caso de uso corresponde a una historia de usuario (HU) y a un requisito funcional (RF) con el mismo número.

```mermaid
flowchart LR
    Recep(["👤 Recepcionista"])

    subgraph SYS["Sistema de Gestión Hotelera"]
        direction TB
        subgraph E1["Épica 1 · Habitaciones"]
            HU01(["HU-01 Registrar habitación"])
            HU02(["HU-02 Modificar habitación"])
            HU03(["HU-03 Eliminar habitación"])
            HU04(["HU-04 Listar habitaciones"])
        end
        subgraph E2["Épica 2 · Huéspedes"]
            HU05(["HU-05 Registrar huésped"])
            HU06(["HU-06 Actualizar huésped"])
            HU07(["HU-07 Eliminar huésped"])
            HU08(["HU-08 Consultar huéspedes"])
        end
        subgraph E3["Épica 3 · Reservas"]
            HU09(["HU-09 Crear reserva"])
            HU10(["HU-10 Cancelar reserva"])
            HU11(["HU-11 Consultar disponibilidad"])
        end
        subgraph E4["Épica 4 · Estadía"]
            HU12(["HU-12 Check-in"])
            SRV(["Registrar servicio adicional"])
            HU13(["HU-13 Check-out"])
        end
        subgraph E5["Épica 5 · Facturación"]
            HU14(["HU-14 Generar factura"])
            PAY(["Registrar pago"])
        end
    end

    Recep --- HU01 & HU02 & HU03 & HU04
    Recep --- HU05 & HU06 & HU07 & HU08
    Recep --- HU09 & HU10 & HU11
    Recep --- HU12 & SRV & HU13
    Recep --- HU14 & PAY

    HU09 -. "«include»" .-> HU11
    HU13 -. "«include» verificar cuenta saldada" .-> PAY
    PAY -. "«include»" .-> HU14
    SRV -. "«extend»" .-> HU14
```

---

## 2. Diagrama de clases

Herencia e interfaces: `IRoom` ← `Room` (abstracta) ← `SingleRoom` / `DoubleRoom` / `SuiteRoom`; `IGuest` + `Person` (abstracta) ← `Guest`. `Hotel` concentra las reglas de negocio y depende de las interfaces (principio de inversión de dependencias). El dinero usa `BigDecimal` (`DECIMAL` en la BD) y los estados son enums.

```mermaid
classDiagram
    direction TB

    class RoomType {
        <<enumeration>>
        SINGLE
        DOUBLE
        SUITE
    }
    class RoomStatus {
        <<enumeration>>
        AVAILABLE
        OCCUPIED
        MAINTENANCE
    }
    class ReservationStatus {
        <<enumeration>>
        PENDING
        ACTIVE
        COMPLETED
        CANCELLED
        +blocksRoom() boolean
    }
    class PaymentMethod {
        <<enumeration>>
        CASH
        CARD
        TRANSFER
    }

    class IRoom {
        <<interface>>
        +getNumber() int
        +getType() RoomType
        +getCapacity() int
        +getPricePerNight() BigDecimal
        +getExtraServiceFee() BigDecimal
        +getStatus() RoomStatus
        +changeStatus(RoomStatus) void
        +setPricePerNight(BigDecimal) void
        +isBookable() boolean
        +calculatePrice(long nights) BigDecimal
    }
    class Room {
        <<abstract>>
        -number: int
        -capacity: int
        -pricePerNight: BigDecimal
        -status: RoomStatus
        +calculatePrice(long nights) BigDecimal
    }
    class SingleRoom
    class DoubleRoom
    class SuiteRoom {
        -extraServiceFee: BigDecimal
        +getExtraServiceFee() BigDecimal
    }
    class RoomFactory {
        +create(RoomType, int number, int capacity, BigDecimal price, BigDecimal fee)$ Room
    }

    class IGuest {
        <<interface>>
        +getDocument() String
        +getFullName() String
        +getEmail() String
        +getPhone() String
    }
    class Person {
        <<abstract>>
        -document: String
        -fullName: String
        -email: String
        -phone: String
    }
    class Guest {
        +updateContactInfo(String email, String phone) void
        +updatePersonalInfo(String name, String email, String phone) void
    }

    class Reservation {
        -reservationCode: String
        -checkInDate: LocalDate
        -checkOutDate: LocalDate
        -status: ReservationStatus
        +overlaps(LocalDate, LocalDate) boolean
        +getStayDays() long
        +checkIn(LocalDate today) void
        +addService(AdditionalService) void
        +getServicesTotal() BigDecimal
        +checkOut() void
        +cancel() void
    }
    class AdditionalService {
        <<immutable>>
        -description: String
        -amount: BigDecimal
        -registeredAt: LocalDateTime
    }
    class Invoice {
        <<immutable>>
        -invoiceId: int
        -stayDays: long
        -nightRate: BigDecimal
        -extraServiceFee: BigDecimal
        -additionalServices: BigDecimal
        -totalAmount: BigDecimal
        -issueDate: LocalDateTime
    }
    class Payment {
        <<immutable>>
        -amount: BigDecimal
        -method: PaymentMethod
        -paidAt: LocalDateTime
    }

    class Hotel {
        -rooms: List~IRoom~
        -guests: List~Guest~
        -reservations: List~Reservation~
        -invoices: List~Invoice~
        -payments: List~Payment~
        +registerRoom(IRoom) void
        +updateRoomRate(int, BigDecimal) void
        +changeRoomType(int, RoomType, int, BigDecimal, BigDecimal) IRoom
        +changeRoomStatus(int, RoomStatus) void
        +deleteRoom(int) void
        +registerGuest(Guest) void
        +updateGuest(String doc, String name, String email, String phone) void
        +deleteGuest(String doc) void
        +searchGuests(String text) List~Guest~
        +createReservation(String doc, int room, LocalDate in, LocalDate out) Reservation
        +getAvailableRooms(LocalDate in, LocalDate out) List~IRoom~
        +cancelReservation(String code) Reservation
        +checkIn(String code) Reservation
        +addService(String code, String description, BigDecimal amount) AdditionalService
        +generateInvoice(String code) Invoice
        +registerPayment(int invoiceId, PaymentMethod) Payment
        +checkOut(String code) Reservation
    }

    class BusinessRuleException {
        -ruleCode: String
    }
    class EntityNotFoundException

    IRoom <|.. Room
    Room <|-- SingleRoom
    Room <|-- DoubleRoom
    Room <|-- SuiteRoom
    RoomFactory ..> Room : crea
    IGuest <|.. Guest
    Person <|-- Guest

    Hotel "1" o-- "0..*" IRoom : gestiona
    Hotel "1" o-- "0..*" Guest : gestiona
    Hotel "1" *-- "0..*" Reservation : gestiona
    Hotel "1" *-- "0..*" Invoice : emite
    Hotel "1" *-- "0..*" Payment : registra
    Reservation "0..*" --> "1" IGuest : huésped
    Reservation "0..*" --> "1" IRoom : habitación
    Reservation "1" *-- "0..*" AdditionalService : servicios
    Invoice "0..1" --> "1" Reservation : factura de
    Payment "0..1" --> "1" Invoice : paga
    Hotel ..> BusinessRuleException : lanza
    Hotel ..> EntityNotFoundException : lanza
```

---

## 3. Diagrama de secuencia — Crear reserva (HU-09)

Muestra cómo se validan las reglas RN-05 (sin cruce de fechas) y RN-13 (no reservar en el pasado ni en mantenimiento).

```mermaid
sequenceDiagram
    autonumber
    actor Recep as Recepcionista
    participant CLI as System_hotel (consola)
    participant Hotel
    participant Room as IRoom
    participant Res as Reservation

    Recep->>CLI: Documento, habitación, fecha de entrada y de salida
    CLI->>Hotel: createReservation(doc, room, in, out)
    activate Hotel
    Hotel->>Hotel: findGuest(doc) / findRoom(room)
    alt Huésped o habitación no existen
        Hotel-->>CLI: EntityNotFoundException
        CLI-->>Recep: "... no fue encontrado(a)"
    else Datos encontrados
        Hotel->>Hotel: validar fechas (salida > entrada, entrada ≥ hoy)
        Hotel->>Room: isBookable()
        Room-->>Hotel: false si está en MAINTENANCE (RN-13)
        loop Cada reserva PENDING/ACTIVE de la habitación
            Hotel->>Res: overlaps(in, out)
            Res-->>Hotel: true / false
        end
        alt Hay cruce de fechas (RN-05)
            Hotel-->>CLI: BusinessRuleException("RN-05")
            CLI-->>Recep: "La habitación no está disponible en el rango de fechas seleccionado"
        else Habitación libre
            Hotel->>Res: new Reservation(RES-AAAA-NNN, guest, room, in, out)
            Hotel->>Hotel: guardar reserva y avanzar el consecutivo
            Hotel-->>CLI: Reservation (PENDING)
            CLI-->>Recep: "Reserva creada: RES-AAAA-NNN ..."
        end
    end
    deactivate Hotel
```

---

## 4. Diagrama de actividades — Estadía, facturación y check-out (HU-12 a HU-14)

```mermaid
flowchart TD
    A([Inicio]) --> B[/Código de reserva/]
    B --> C{¿Reserva PENDING<br/>y dentro de su periodo?}
    C -- No --> X1[Mostrar error: check-in no permitido] --> Z([Fin])
    C -- Sí --> D{¿Habitación<br/>AVAILABLE?}
    D -- No --> X2[Mostrar error: habitación no disponible] --> Z
    D -- Sí --> E[Check-in: reserva ACTIVE,<br/>habitación OCCUPIED]
    E --> F{¿El huésped consumió<br/>un servicio?}
    F -- Sí --> G[Registrar servicio adicional] --> F
    F -- No / fin de la estadía --> H[Generar factura:<br/>noches × tarifa + cargo Suite + servicios]
    H --> I[Mostrar comprobante detallado]
    I --> J{¿Pago registrado<br/>por el total?}
    J -- No --> X3[Check-out bloqueado:<br/>saldo pendiente RN-09] --> J
    J -- Sí --> K[Check-out: reserva COMPLETED]
    K --> L[Habitación MAINTENANCE<br/>Mantenimiento/Limpieza RN-10]
    L --> M[Recepcionista habilita la<br/>habitación como AVAILABLE]
    M --> Z
```
