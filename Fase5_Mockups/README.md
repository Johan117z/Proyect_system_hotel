# Phase 5: Mockups & Interactive Prototype

## 📌 Relationship to Phase 2 (SRS)

The SRS v2.0 (`Fase2_Requerimientos/SRS.md`) defines the system as a **Java console (CLI) application** (restriction **RS-01**). The material in this folder is a **proposal for how the project could evolve**: a graphical interface for a later version. It is not the console deliverable required by the SRS.

The prototype follows the same model as the SRS v2.0, the database (Phase 4) and the Java code (Phase 6): the 14 user stories, the same business rules (RN-01 to RN-15) and the same seed data.

## 📂 Contents of this folder

| File | Description |
| :--- | :--- |
| `index.html` | Interactive HTML/JS prototype ("Gran Continental") covering the 14 user stories / RF-01 to RF-14 in the browser |
| `Mockup Hotel.md` | Link to the first visual exploration made in Figma |

## ▶️ How to open the prototype

Open `index.html` in any modern browser. No installation or server is needed; data is kept in the browser's `localStorage` (the **Restablecer datos demo** option reloads the seed data).

On the login screen, use the **quick access** buttons to enter as one of two demo users:

| Role | What it can do |
| :--- | :--- |
| Administrator | Everything, including deleting rooms and guests, changing rates and room status, and resetting the demo data |
| Receptionist | Rooms (edit, enable after cleaning), guests, reservations, check-in / check-out, services, invoices and payments |

The login only demonstrates **role-based access control**. Credentials are stored in the page itself and are not secure. Risk **R-02 (no access control)** of the SRS stays open until authentication is implemented in the application.

## 🖥️ Screens

| Screen | User stories |
| :--- | :--- |
| Panel general (dashboard) | HU-04 |
| Habitaciones | HU-01 to HU-04 |
| Huéspedes | HU-05 to HU-08 |
| Reservas | HU-09, HU-10 |
| Disponibilidad | HU-11 |
| Estadías (check-in, services, invoice, payment, check-out) | HU-12 to HU-14 |
| Facturación | HU-14 |

## ✅ Alignment with the model

| Rule | In the prototype |
| :--- | :--- |
| Guest data (RF-05) | Document 6–10 digits, phone 10 digits, valid email; full name (first + last names) up to 60 characters |
| Rooms (RF-01) | Number 1–65535, type Sencilla / Doble / Suite, capacity, rate and extra fee (Suite only) |
| Reservation code | `RES-YYYY-NNN` |
| No overlap, no past dates, no rooms in maintenance (RN-05, RN-13) | Checked when creating a reservation and in the availability search |
| Check-in (RN-08) | Pending reservation, within its period, room available |
| Billing (RN-11, RN-12) | Reservation nights × rate + Suite fee + services; the invoice is issued during the stay, cannot be changed or voided, and no services can be added after it |
| Check-out (RN-09, RN-10) | Only with the invoice paid; the room goes to *Mantenimiento/Limpieza* |
| Currency | `$ 1.830.000 COP` (Colombian format) |

The seed stay (Luis Fernando Torres, Suite 201, 5 nights, Lavandería $30.000) produces the same invoice as the database and the Java application: **$ 1.830.000 COP**.

## 🔤 Labels used by the interface

The interface shows Spanish labels for the model states:

| Model | Interface |
| :--- | :--- |
| Room `AVAILABLE` / `OCCUPIED` / `MAINTENANCE` | Disponible / Ocupada / Mantenimiento-Limpieza |
| Reservation `PENDING` / `ACTIVE` / `COMPLETED` / `CANCELLED` | Pendiente / Activa / Completada / Cancelada |
| Payment `CASH` / `CARD` / `TRANSFER` | Efectivo / Tarjeta / Transferencia |

## 🔜 What's missing for this to become a formal GUI deliverable

- Update **RS-01** in the SRS to include a graphical interface.
- Implement real authentication (close risk **R-02**).
- Choose the GUI technology: Java Swing/JavaFX if the SRS language is kept, or justify a change of stack if web technologies are used.
- Connect the interface to the application and the database instead of `localStorage`.
