# Historias de Usuario — Sistema de Gestión Hotelera

Versión en Markdown del documento formal `Historia_Usuario_Gestion_Hotel.pdf` (Fase 1). Contiene las mismas 14 historias, reglas de negocio y criterios de aceptación.

## 1. Problema

El hotel requiere gestionar en un mismo sistema las habitaciones y los huéspedes, las reservas y la disponibilidad, el ingreso y la salida de huéspedes y la facturación de las estadías. Sin una gestión integrada es difícil mantener actualizados los estados de las habitaciones, evitar cruces de reservas, conservar datos consistentes y realizar el cobro correspondiente.

## 2. Objetivo

Desarrollar un sistema que permita a la recepción administrar habitaciones y huéspedes, crear y consultar reservas, controlar la disponibilidad, registrar check-in y check-out y generar facturas, aplicando las reglas de negocio definidas.

## 3. Actores

- **Recepcionista:** registra y consulta habitaciones y huéspedes, gestiona reservas, realiza check-in y check-out y genera facturas.
- **Sistema de gestión del hotel:** mantiene estados, valida restricciones, controla la disponibilidad y calcula la facturación.

## 4. Épicas

| Épica | Alcance | Historias |
| :--- | :--- | :--- |
| Épica 1 — Habitaciones | Registro, modificación, eliminación y consulta de habitaciones y sus estados. | HU-01 a HU-04 |
| Épica 2 — Huéspedes | Registro, actualización, eliminación y consulta de huéspedes. | HU-05 a HU-08 |
| Épica 3 — Reservas | Crear, cancelar y consultar reservas y disponibilidad por fechas. | HU-09 a HU-11 |
| Épica 4 — Estadía | Ingreso y salida de huéspedes, con cambio de estado de la habitación. | HU-12, HU-13 |
| Épica 5 — Facturación | Cálculo y generación de la factura de la estadía y sus servicios adicionales. | HU-14 |

## 5. MVP y backlog

**MVP (Must Have):** HU-01, HU-04, HU-05, HU-08, HU-09, HU-11, HU-12, HU-13 y HU-14.

| Sprint | Historias |
| :--- | :--- |
| Sprint 1 — Base del sistema | HU-01 (Must), HU-04 (Must), HU-05 (Must), HU-08 (Must) |
| Sprint 2 — Reservas y disponibilidad | HU-09 (Must), HU-11 (Must), HU-10 (Should) |
| Sprint 3 — Estadía y cobro | HU-12 (Must), HU-13 (Must), HU-14 (Must) |
| Sprint 4 — Mantenimiento de datos | HU-02 (Should), HU-06 (Should), HU-03 (Could), HU-07 (Could) |

Priorización MoSCoW: Must = imprescindible para el MVP; Should = importante después del flujo mínimo; Could = útil pero postergable.

## 6. Reglas de negocio

- El número de habitación debe ser único.
- El documento del huésped debe ser único.
- Una habitación no puede tener reservas con cruce de fechas.
- No se elimina una habitación ocupada ni con reservas futuras asociadas.
- No se elimina un huésped con hospedaje activo, reservas vigentes o facturas sin pagar.
- El check-in cambia la habitación a estado "Ocupada".
- El check-out requiere la cuenta saldada y cambia la habitación a "Mantenimiento/Limpieza".
- La factura se calcula como Días de estadía × Tarifa por noche + Servicios adicionales.

El SRS v2.0 (`Fase2_Requerimientos/SRS.md`) numera estas reglas como RN-01 a RN-11 y agrega RN-12 a RN-15.

---

## 7. Historias de usuario y criterios de aceptación

### Épica 1 — Habitaciones

#### HU-01 — Registro de habitaciones · *Must Have*

**Como** recepcionista, **quiero** registrar habitaciones, **para** tener un control de las habitaciones disponibles en el hotel.

- **Dado que** el recepcionista completa número, tipo, capacidad y precio por noche con valores válidos, **cuando** presione guardar, **entonces** el sistema guarda la habitación y la establece en estado "Disponible".
- **Dado que** el recepcionista intenta registrar un número de habitación ya existente, **cuando** presione guardar, **entonces** el sistema muestra el error: "El número de habitación ya se encuentra registrado".

#### HU-02 — Modificación de habitaciones · *Should Have*

**Como** recepcionista, **quiero** modificar los datos de una habitación, **para** mantener actualizada su información.

- **Dado que** la habitación existe y el recepcionista la busca por su número, **cuando** edite tipo, capacidad o tarifa por noche con valores válidos y guarde, **entonces** el sistema actualiza los datos de la habitación.
- **Dado que** el número no corresponde a una habitación registrada, **cuando** el recepcionista intente buscarla para modificarla, **entonces** el sistema informa que la habitación no fue encontrada y no realiza cambios.

#### HU-03 — Eliminación de habitaciones · *Could Have*

**Como** recepcionista, **quiero** eliminar una habitación, **para** retirar del sistema las habitaciones que ya no estén disponibles.

- **Dado que** la habitación no está ocupada ni tiene reservas futuras asociadas, **cuando** el recepcionista solicite su eliminación y confirme la acción, **entonces** el sistema elimina la habitación del registro.
- **Dado que** la habitación está ocupada o tiene una reserva futura asociada, **cuando** el recepcionista intente eliminarla, **entonces** el sistema bloquea la acción y muestra la restricción correspondiente.

#### HU-04 — Listado de habitaciones · *Must Have*

**Como** recepcionista, **quiero** listar las habitaciones, **para** consultar las habitaciones registradas en el sistema.

- **Dado que** existen habitaciones registradas, **cuando** el recepcionista solicite el listado, **entonces** el sistema muestra cada habitación con número, tipo y estado actual (Disponible, Ocupada, Mantenimiento).
- **Dado que** no existen habitaciones registradas, **cuando** el recepcionista solicite el listado, **entonces** el sistema informa que no hay habitaciones registradas.

### Épica 2 — Huéspedes

#### HU-05 — Registro de huéspedes · *Must Have*

**Como** recepcionista, **quiero** registrar huéspedes, **para** tener sus datos almacenados en el sistema.

- **Dado que** el recepcionista ingresa nombre, documento, correo electrónico y teléfono del huésped, y el documento no está registrado, **cuando** confirme el registro, **entonces** el sistema crea el perfil del huésped y almacena la información ingresada.
- **Dado que** el número de documento ya existe, **cuando** el recepcionista intente registrar el huésped, **entonces** el sistema rechaza el registro y muestra el mensaje: "El huésped ya existe en el sistema".

#### HU-06 — Actualización de datos de huésped · *Should Have*

**Como** recepcionista, **quiero** actualizar los datos de un huésped, **para** mantener su información correcta y actualizada.

- **Dado que** el huésped está registrado, **cuando** el recepcionista edite su información personal o de contacto con datos válidos y guarde, **entonces** el sistema actualiza los datos del huésped.
- **Dado que** el huésped no está registrado, **cuando** el recepcionista intente actualizarlo, **entonces** el sistema informa que el huésped no fue encontrado y no realiza cambios.

#### HU-07 — Eliminación de huésped · *Could Have*

**Como** recepcionista, **quiero** eliminar un huésped, **para** retirar del sistema los registros que ya no sean necesarios.

- **Dado que** el huésped no tiene hospedaje activo, reservas vigentes ni facturas sin pagar, **cuando** el recepcionista solicite y confirme la eliminación, **entonces** el sistema elimina el registro del huésped.
- **Dado que** el huésped tiene hospedaje activo, reservas vigentes o facturas sin pagar, **cuando** el recepcionista intente eliminarlo, **entonces** el sistema restringe la eliminación.

#### HU-08 — Consulta de huéspedes · *Must Have*

**Como** recepcionista, **quiero** consultar huéspedes, **para** buscar y revisar la información de los huéspedes registrados.

- **Dado que** existen huéspedes registrados y el recepcionista ingresa un nombre, apellido o documento, **cuando** ejecute la búsqueda, **entonces** el sistema muestra los resultados coincidentes.
- **Dado que** no existen huéspedes que coincidan con el criterio, **cuando** el recepcionista ejecute la búsqueda, **entonces** el sistema informa que no se encontraron resultados.

### Épica 3 — Reservas

#### HU-09 — Creación de reserva · *Must Have*

**Como** recepcionista, **quiero** crear una reserva, **para** asignar una habitación a un huésped durante un período determinado.

- **Dado que** la habitación está disponible durante todo el rango de fechas solicitado y el huésped está registrado, **cuando** el recepcionista confirme la reserva, **entonces** el sistema registra la reserva y asigna la habitación al huésped para ese período.
- **Dado que** existe un cruce de fechas con otra reserva, **cuando** el recepcionista intente confirmar la reserva, **entonces** el sistema muestra el error: "La habitación no está disponible en el rango de fechas seleccionado".

#### HU-10 — Cancelación de reserva · *Should Have*

**Como** recepcionista, **quiero** cancelar una reserva, **para** liberar la habitación cuando el huésped ya no la necesite.

- **Dado que** existe una reserva activa (pendiente de check-in), **cuando** el recepcionista solicite su cancelación y confirme la acción, **entonces** el sistema anula la reserva y libera la disponibilidad de la habitación en esas fechas.
- **Dado que** la reserva no existe o ya está cancelada, **cuando** el recepcionista intente cancelarla, **entonces** el sistema informa que no puede realizar la cancelación.

#### HU-11 — Consulta de disponibilidad · *Must Have*

**Como** recepcionista, **quiero** consultar las habitaciones disponibles y ocupadas, **para** saber qué habitaciones puedo asignar a los huéspedes.

- **Dado que** existen habitaciones y reservas registradas, **cuando** el recepcionista consulte un rango de fechas, **entonces** el sistema muestra únicamente las habitaciones que no presenten cruce de fechas con reservas.
- **Dado que** una habitación presenta cruce de fechas con una reserva, **cuando** el recepcionista consulte ese rango, **entonces** el sistema excluye esa habitación de las disponibles.

### Épica 4 — Estadía

#### HU-12 — Registro de check-in · *Must Have*

**Como** recepcionista, **quiero** realizar el check-in de un huésped, **para** registrar oficialmente su llegada al hotel.

- **Dado que** el huésped tiene una reserva válida y una habitación asignada, **cuando** el recepcionista registre el check-in, **entonces** el sistema registra la llegada y actualiza el estado de la habitación a "Ocupada".
- **Dado que** no existe una reserva válida o una habitación asignada para el huésped, **cuando** el recepcionista intente realizar el check-in, **entonces** el sistema impide el registro del ingreso.

#### HU-13 — Registro de check-out · *Must Have*

**Como** recepcionista, **quiero** realizar el check-out de un huésped, **para** registrar su salida y liberar la habitación.

- **Dado que** el huésped tiene un hospedaje activo y su cuenta está saldada, **cuando** el recepcionista registre el check-out, **entonces** el sistema registra la salida y cambia la habitación a estado "Mantenimiento/Limpieza".
- **Dado que** la cuenta del huésped tiene un saldo pendiente, **cuando** el recepcionista intente realizar el check-out, **entonces** el sistema impide finalizar la salida hasta que la cuenta esté saldada.

### Épica 5 — Facturación

#### HU-14 — Generación de factura · *Must Have*

**Como** recepcionista, **quiero** generar una factura, **para** registrar y entregar el cobro correspondiente a la estadía del huésped.

- **Dado que** existe una estadía con días, tarifa por noche y servicios adicionales registrados, **cuando** el recepcionista genere la factura, **entonces** el sistema calcula el total como Días de estadía × Tarifa por noche + Servicios adicionales y genera un comprobante detallado.
- **Dado que** no existen datos suficientes para calcular la estadía o los servicios, **cuando** el recepcionista intente generar la factura, **entonces** el sistema impide la generación hasta completar la información requerida.

---

## 8. Trazabilidad

| Historias | Clases Java (Fase 6) | Responsabilidad |
| :--- | :--- | :--- |
| HU-01 a HU-04 | `IRoom`, `Room`, `SingleRoom`, `DoubleRoom`, `SuiteRoom`, `RoomFactory`, `Hotel` | Registro, modificación, eliminación y listado de habitaciones. |
| HU-05 a HU-08 | `IGuest`, `Person`, `Guest`, `Hotel` | Registro, actualización, eliminación y consulta de huéspedes. |
| HU-09 a HU-11 | `Reservation`, `IRoom`, `Hotel` | Reservas y validación de disponibilidad por fechas. |
| HU-12, HU-13 | `Reservation`, `IRoom`, `Hotel` | Check-in, check-out y transición de estados. |
| HU-14 | `AdditionalService`, `Invoice`, `Payment`, `Hotel` | Servicios, cálculo y generación de la factura, y pago. |

La matriz completa (HU → RF → regla → clase → tabla) está en `docs/MATRIZ_TRAZABILIDAD.md`.
