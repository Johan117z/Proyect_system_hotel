# Especificación de Requisitos de Software (SRS)

## Sistema de Gestión Hotelera (`System_hotel`)

| | |
| :--- | :--- |
| **Estándar** | IEEE Std 830-1998 |
| **Versión** | 2.0 (reemplaza la v1.0 en PDF: `SRS_Sistema_Gestion_Hotelera_Fase2.0.pdf`) |
| **Aprendices** | Mateo Esteban Ramírez Garzón · Johan Andrés Liñán Esquivel |
| **Instructor** | Carlos Julio |
| **Programa** | Análisis y Desarrollo de Software (3239188) — SENA, Centro de la Industria, la Empresa y los Servicios, Neiva |
| **Año** | 2026 |

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica de manera formal, completa y verificable los requisitos del Sistema de Gestión Hotelera, siguiendo la estructura del estándar IEEE 830-1998. Está dirigido a los aprendices desarrolladores, al instructor evaluador y a cualquier persona interesada en validar el alcance funcional y técnico del sistema.

La versión 2.0 conserva los 14 requisitos funcionales de la v1.0 (uno por cada historia de usuario de la Fase 1) y los alinea con lo construido en las fases siguientes: diagramas UML (Fase 3), base de datos relacional (Fase 4), prototipo de interfaz (Fase 5) y aplicación Java (Fase 6). La sección 4 resume los cambios.

### 1.2 Alcance del sistema

El Sistema de Gestión Hotelera es una aplicación de consola (CLI) desarrollada en Java que permite a un recepcionista gestionar habitaciones, huéspedes, reservas, check-in, servicios adicionales, check-out, facturación y pagos de un hotel. La aplicación mantiene la información en memoria (`List`/`ArrayList`). El modelo relacional de la Fase 4 queda diseñado y probado para una futura capa de persistencia, pero no se conecta en esta versión.

Su propósito es estandarizar los procesos de recepción, evitar datos duplicados y errores de disponibilidad, y asegurar un cobro correcto de cada estadía.

### 1.3 Definiciones, acrónimos y abreviaturas

- **RF:** Requisito Funcional.
- **RNF:** Requisito No Funcional.
- **RN:** Regla de Negocio.
- **RS:** Restricción del sistema.
- **CA:** Criterio de Aceptación.
- **HU:** Historia de Usuario.
- **CU:** Caso de Uso.
- **CLI:** Command Line Interface (interfaz de línea de comandos).
- **JDK / JVM:** Java Development Kit / Java Virtual Machine.
- **POO:** Programación Orientada a Objetos.
- **Estadía:** periodo entre el check-in y el check-out de una reserva.
- **Cuenta saldada:** la factura de la estadía está emitida y pagada en su totalidad.
- **Formato de fecha:** dd/mm/aaaa, uniforme en toda la entrada y salida de datos.
- **Formato monetario:** pesos colombianos (COP), hasta dos decimales, con punto como separador de miles y coma decimal (ej. `$ 1.830.000 COP`, `$ 25.000,50 COP`).

**Estados del sistema** (se usan los mismos valores en Java y en la base de datos):

| Entidad | Valor | Significado |
| :--- | :--- | :--- |
| Habitación | `AVAILABLE` | Disponible |
| Habitación | `OCCUPIED` | Ocupada |
| Habitación | `MAINTENANCE` | Mantenimiento/Limpieza |
| Reserva | `PENDING` | Creada, sin check-in |
| Reserva | `ACTIVE` | Con check-in: estadía en curso |
| Reserva | `COMPLETED` | Con check-out |
| Reserva | `CANCELLED` | Cancelada antes del check-in |

### 1.4 Referencias

- IEEE Std 830-1998 — IEEE Recommended Practice for Software Requirements Specifications.
- Fase 1 — Historias de Usuario, Sistema de Gestión Hotelera (`Fase1_Epicas_Historias/`): 14 HU, reglas de negocio y criterios de aceptación.
- Fase 3 — Diagramas UML (`Fase3_Diagramas_UML/`).
- Fase 4 — Modelo relacional `system_hotel_db` (`Fase4_Base_Datos/`).
- Fase 5 — Prototipo de interfaz (`Fase5_Mockups/`).
- Fase 6 — Aplicación Java (`Fase6_Desarrollo_Funcional/`).
- Matriz de trazabilidad detallada (requisito → clase → tabla): `docs/MATRIZ_TRAZABILIDAD.md`.

### 1.5 Visión general del documento

La sección 2 describe el sistema, sus funciones, actores y restricciones. La sección 3 detalla los requisitos funcionales, no funcionales, interfaces, reglas de negocio y requisitos de datos. El Anexo A contiene los 28 criterios de aceptación (uno positivo y uno negativo por HU); el Anexo B, la matriz de trazabilidad; el Anexo C, los riesgos y limitaciones; el Anexo D, las fichas de casos de uso en formato IEEE 830 (ID, Nombre, Actores, Precondiciones, Flujo Normal, Flujos Alternativos, Flujos Anormales/Excepciones y Postcondiciones). La sección 4 registra el control de cambios.

---

## 2. Descripción general

### 2.1 Perspectiva del producto

Producto independiente y autocontenido, sin integraciones con sistemas externos. Reemplaza procesos manuales o dispersos de recepción por una única aplicación de consola. Su diseño separa el modelo de dominio de la interfaz para facilitar, en fases futuras, la conexión con la base de datos de la Fase 4 y una interfaz gráfica como la propuesta en la Fase 5.

### 2.2 Funciones del producto

- Gestión de habitaciones: registro, modificación, eliminación y listado.
- Gestión de huéspedes: registro, actualización, eliminación y consulta.
- Gestión de reservas: creación, cancelación y consulta de disponibilidad por fechas.
- Gestión de estadías: check-in, registro de servicios adicionales y check-out.
- Facturación: generación de la factura de la estadía y registro de su pago.

### 2.3 Características de los usuarios / Actores

| Actor | Tipo | Participación |
| :--- | :--- | :--- |
| Recepcionista | Principal | Interactúa directamente con el sistema: registra y consulta habitaciones y huéspedes, gestiona reservas, realiza check-in y check-out, registra servicios, genera facturas y registra pagos. |
| Huésped | Secundario / pasivo | No opera el sistema. Es el sujeto de los datos (nombre, documento, contacto) y el origen de las solicitudes que el recepcionista registra. |
| Sistema | Interno | Mantiene estados, valida restricciones, controla la disponibilidad y calcula la facturación. |

### 2.4 Restricciones generales

- **RS-01:** La aplicación se desarrolla en Java y se ejecuta mediante interfaz de consola (CLI). La interfaz gráfica de la Fase 5 es una propuesta para una versión futura y no forma parte de este alcance.
- **RS-02:** La aplicación gestiona la información en memoria (`List`/`ArrayList`). El modelo relacional de la Fase 4 (MySQL 8) queda diseñado, documentado y probado, pero la aplicación no se conecta a él en esta versión (ver 3.5 y riesgo R-01).
- **RS-03:** Los identificadores del código (clases, métodos, atributos y variables) se escriben en inglés. Los comentarios y los mensajes que ve el usuario se escriben en español, el idioma de los usuarios y del equipo. *(Cambio frente a la v1.0, que pedía también comentarios en inglés.)*
- **RS-04:** Solo se usan las librerías estándar del JDK, sin frameworks ni dependencias externas.
- **RS-05:** El diseño mantiene responsabilidades claras en las clases, sin concentrar la lógica del sistema en una sola clase.
- **RS-06:** Fechas y valores monetarios usan el formato único definido en 1.3, para evitar ambigüedades en los cálculos de facturación y disponibilidad.

### 2.5 Supuestos y dependencias

- Un único recepcionista opera el sistema por sesión, sin concurrencia ni control de acceso (ver riesgos R-02 y R-04).
- El equipo de ejecución cuenta con una JVM compatible con **Java 17** o superior (versión LTS usada por el proyecto Maven de la Fase 6).
- Al no existir persistencia en la aplicación (RS-02), la información se pierde al cerrar el programa (ver riesgo R-01).

---

## 3. Requisitos específicos

### 3.1 Requisitos funcionales

Cada requisito corresponde a una de las 14 historias de usuario de la Fase 1 (RF-*n* ↔ HU-*n*). Cada descripción incluye el comportamiento ante datos válidos y ante la condición de error, para cumplir el criterio de verificabilidad del estándar IEEE 830.

#### Épica 1 — Habitaciones

| ID | Actor | Descripción |
| :--- | :--- | :--- |
| **RF-01** | Recepcionista | El sistema debe permitir registrar una habitación ingresando número (1 a 65535), tipo (Sencilla, Doble o Suite), capacidad y tarifa por noche mayor que cero; para la Suite, además, un cargo extra fijo por estadía. La habitación queda en estado Disponible. Si el número ya existe, el sistema debe rechazar el registro con el mensaje "El número de habitación ya se encuentra registrado" (RN-01). |
| **RF-02** | Recepcionista | El sistema debe permitir buscar una habitación por su número y modificar su tipo, capacidad o tarifa por noche. Si el número no corresponde a una habitación registrada, debe informar que no fue encontrada y no aplicar cambios. El tipo solo puede cambiar si la habitación no tiene reservas pendientes ni activas (RN-14). |
| **RF-03** | Recepcionista | El sistema debe permitir eliminar una habitación, previa confirmación, únicamente cuando no esté ocupada ni tenga reservas futuras asociadas (RN-02); en caso contrario debe bloquear la acción e informar la restricción. La eliminación es lógica: la habitación sale del inventario y su historial de reservas y facturas se conserva. |
| **RF-04** | Recepcionista | El sistema debe permitir listar las habitaciones mostrando número, tipo, capacidad, tarifa y estado actual (Disponible, Ocupada, Mantenimiento/Limpieza), y filtrar por estado. Si no hay habitaciones registradas, debe informarlo explícitamente. |

#### Épica 2 — Huéspedes

| ID | Actor | Descripción |
| :--- | :--- | :--- |
| **RF-05** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir registrar un huésped con nombre completo (máx. 60 caracteres), documento (6 a 10 dígitos), correo electrónico (máx. 80 caracteres, formato válido) y teléfono (10 dígitos), garantizando que el documento sea único (RN-03). Si ya existe, debe rechazar el registro con el mensaje "El huésped ya existe en el sistema". |
| **RF-06** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir editar el nombre y los datos de contacto de un huésped registrado; el documento no se modifica. Si el huésped no está registrado, debe informarlo y no aplicar cambios. |
| **RF-07** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir eliminar un huésped, previa confirmación, únicamente cuando no tenga hospedaje activo, reservas vigentes ni facturas sin pagar (RN-04); en caso contrario debe restringir la eliminación. La eliminación es lógica y conserva el historial. |
| **RF-08** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir buscar huéspedes por nombre, apellido o documento, sin distinguir mayúsculas ni tildes, y mostrar los resultados coincidentes. Si no hay coincidencias, debe informar que no se encontraron resultados. |

#### Épica 3 — Reservas

| ID | Actor | Descripción |
| :--- | :--- | :--- |
| **RF-09** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir crear una reserva asociando un huésped registrado con una habitación durante un periodo, verificando que la fecha de salida sea posterior a la de entrada, que la entrada no sea anterior a hoy, que la habitación no esté en mantenimiento (RN-13) y que no exista cruce de fechas (RN-05). La reserva queda en estado Pendiente con un código `RES-AAAA-NNN`. En caso de cruce, debe rechazarla con el mensaje "La habitación no está disponible en el rango de fechas seleccionado". |
| **RF-10** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir cancelar, previa confirmación, una reserva pendiente y liberar la disponibilidad de la habitación en esas fechas (RN-06). Si la reserva no existe o ya fue cancelada (o ya tuvo check-in), debe informar que no puede realizar la cancelación. |
| **RF-11** | Recepcionista | El sistema debe permitir consultar las habitaciones disponibles para un rango de fechas, excluyendo las que tienen cruce con reservas pendientes o activas y las que están en mantenimiento (RN-07). Si ninguna está disponible, debe informarlo. |

#### Épica 4 — Estadía

| ID | Actor | Descripción |
| :--- | :--- | :--- |
| **RF-12** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir realizar el check-in de una reserva pendiente, desde su fecha de entrada y hasta la noche anterior a su salida, cuando la habitación asignada esté Disponible. La reserva pasa a Activa y la habitación a Ocupada (RN-08). Si no existe una reserva válida o la habitación no está disponible, debe impedir el check-in. |
| **RF-13** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | El sistema debe permitir realizar el check-out de una estadía activa solo si la cuenta está saldada, es decir, si su factura fue emitida y pagada (RN-09). La reserva pasa a Completada y la habitación a Mantenimiento/Limpieza (RN-10); después, el recepcionista la habilita como Disponible. Si existe saldo pendiente, debe impedir el check-out e informar el valor. |

#### Épica 5 — Facturación

| ID | Actor | Descripción |
| :--- | :--- | :--- |
| **RF-14** | Recepcionista (ejecuta) / Huésped (sujeto de datos) | Durante una estadía activa, el sistema debe permitir registrar servicios adicionales (descripción y valor mayor que cero) y generar la factura, calculando el total como noches de la reserva × tarifa por noche + cargo extra de la habitación (solo Suite) + servicios adicionales (RN-11), con un comprobante detallado. Una estadía tiene una sola factura, que no se modifica una vez emitida (RN-12). El sistema debe permitir registrar el pago total de la factura (efectivo, tarjeta o transferencia). Si no hay una estadía activa con datos suficientes, debe impedir la generación. |

### 3.2 Requisitos no funcionales

| ID | Atributo | Descripción |
| :--- | :--- | :--- |
| **RNF01** | Mantenibilidad | El código sigue principios de POO (encapsulamiento, herencia, polimorfismo, interfaces) y SOLID, organizado en modelo, servicio (`Hotel`) e interfaz de consola, sin lógica de negocio en la capa de presentación. |
| **RNF02** | Mantenibilidad | Los identificadores del código están en inglés y siguen las convenciones de Java (camelCase, PascalCase), conforme a RS-03. |
| **RNF03** | Usabilidad | Todos los menús presentan opciones numeradas de forma secuencial e incluyen siempre una opción para volver. El 100 % de las opciones mostradas tiene una acción implementada. Las acciones que no se pueden deshacer (eliminar, cancelar) piden confirmación. |
| **RNF04** | Confiabilidad | El sistema valida toda entrada (tipo de dato, campos vacíos, rangos, formatos) y captura las excepciones de forma controlada, de modo que ningún dato inválido cierre la aplicación. |
| **RNF05** | Rendimiento | Búsquedas, consultas, registros y cálculo de disponibilidad responden en menos de 1 segundo para un volumen de hasta 500 registros en memoria, medido con prueba manual. |
| **RNF06** | Portabilidad | La aplicación compila y se ejecuta en una JVM compatible con **Java 17** o superior, usando solo librerías estándar del JDK (RS-04). *(La v1.0 indicaba Java 8; se actualizó porque el proyecto usa construcciones de Java 17, versión LTS.)* |
| **RNF07** | Consistencia de datos | Fechas en formato dd/mm/aaaa y valores monetarios en COP con hasta dos decimales, de forma uniforme en toda la aplicación (RS-06). Los montos se manejan con `BigDecimal` para evitar errores de redondeo. |

### 3.3 Interfaces externas

El sistema no tiene interfaces con hardware, software de terceros ni comunicación en red. La única interfaz de usuario es la consola (RS-01).

### 3.4 Reglas de negocio

RN-01 a RN-11 provienen de la Fase 1 y de la v1.0. RN-12 a RN-15 se agregaron en la v2.0 a partir del diseño de la base de datos y del código.

| ID | Descripción |
| :--- | :--- |
| **RN-01** | El número de habitación es único y no puede registrarse nuevamente, ni siquiera el de una habitación eliminada. |
| **RN-02** | Una habitación solo puede eliminarse si no está ocupada ni tiene reservas futuras (pendientes o activas). |
| **RN-03** | El documento de identidad de cada huésped es único e irrepetible. |
| **RN-04** | Un huésped no puede eliminarse si tiene hospedaje activo, reservas vigentes o facturas sin pagar. |
| **RN-05** | No se puede crear una reserva si la habitación tiene cruce de fechas con otra reserva pendiente o activa, ni si la fecha de salida es igual o anterior a la de entrada. La salida es exclusiva: un huésped puede entrar el mismo día en que otro sale. |
| **RN-06** | Cancelar una reserva pendiente libera la disponibilidad de la habitación para esas fechas. |
| **RN-07** | La consulta de disponibilidad considera las fechas solicitadas y excluye las habitaciones con cruce de reservas y las que están en mantenimiento. |
| **RN-08** | El check-in exige una reserva pendiente, dentro de su periodo, y una habitación Disponible; al realizarlo, la habitación pasa a Ocupada. |
| **RN-09** | El check-out exige la cuenta saldada: factura emitida y pagada en su totalidad. |
| **RN-10** | Después del check-out, la habitación queda en estado Mantenimiento/Limpieza. |
| **RN-11** | Total de la factura = noches de la reserva × tarifa por noche + cargo extra de la habitación (solo Suite) + servicios adicionales. |
| **RN-12** | Una factura emitida es inmutable: no se modifica, no se anula ni se elimina, y no se agregan servicios a una estadía ya facturada. Cada estadía tiene como máximo una factura. |
| **RN-13** | No se puede reservar con fecha de entrada anterior a hoy ni una habitación en mantenimiento. |
| **RN-14** | El tipo de una habitación solo puede cambiar si no tiene reservas pendientes ni activas. |
| **RN-15** | El estado Ocupada solo lo asigna el check-in y solo lo libera el check-out; el cambio manual de estado se limita a Disponible ↔ Mantenimiento/Limpieza. |

### 3.5 Requisitos de datos

La aplicación mantiene los datos en memoria durante la ejecución (RS-02). Las restricciones de los datos son las mismas en Java (validaciones de las clases) y en el modelo relacional de la Fase 4:

| Dato | Regla |
| :--- | :--- |
| Documento del huésped | 6 a 10 dígitos, único |
| Nombre completo | Obligatorio, máximo 60 caracteres |
| Correo electrónico | Obligatorio, formato válido, máximo 80 caracteres |
| Teléfono | 10 dígitos (celular colombiano, sin `+57`) |
| Número de habitación | 1 a 65535, único |
| Capacidad | 1 a 255 huéspedes |
| Tarifa por noche | Mayor que cero, COP |
| Cargo extra | Mayor o igual a cero; solo en Suite |
| Código de reserva | `RES-AAAA-NNN` (año de entrada y consecutivo) |
| Servicio adicional | Descripción obligatoria (máx. 80 caracteres), valor mayor que cero |
| Medio de pago | Efectivo, Tarjeta o Transferencia |

El modelo relacional (tablas `guests`, `rooms`, `reservations`, `additional_services`, `invoices` y `payments`, con restricciones `CHECK`, `UNIQUE`, claves foráneas y triggers para RN-09 y RN-12) está en `Fase4_Base_Datos/`.

---

## Anexo A. Criterios de aceptación

Un criterio positivo y uno negativo por cada historia de usuario, alineados uno a uno con la Fase 1.

| ID | HU | Escenario | Resultado esperado |
| :--- | :--- | :--- | :--- |
| CA-01 | HU-01 | Registrar una habitación con número, tipo, capacidad y tarifa por noche válidos. | El sistema guarda la habitación en estado Disponible. |
| CA-02 | HU-01 | Registrar un número de habitación ya existente. | El sistema muestra: "El número de habitación ya se encuentra registrado". |
| CA-03 | HU-02 | Buscar una habitación por número y editar tipo, capacidad o tarifa con valores válidos. | El sistema actualiza los datos de la habitación. |
| CA-04 | HU-02 | Modificar una habitación cuyo número no corresponde a ninguna registrada. | El sistema informa que la habitación no fue encontrada y no realiza cambios. |
| CA-05 | HU-03 | Eliminar y confirmar una habitación que no está ocupada ni tiene reservas futuras. | El sistema elimina la habitación del inventario. |
| CA-06 | HU-03 | Eliminar una habitación ocupada o con reservas futuras. | El sistema bloquea la eliminación y muestra la restricción. |
| CA-07 | HU-04 | Consultar el listado cuando existen habitaciones registradas. | El sistema muestra número, tipo y estado actual de cada habitación. |
| CA-08 | HU-04 | Consultar el listado sin habitaciones registradas. | El sistema informa que no hay habitaciones registradas. |
| CA-09 | HU-05 | Registrar un huésped con datos válidos y documento no registrado. | El sistema crea el perfil del huésped. |
| CA-10 | HU-05 | Registrar un huésped con documento ya existente. | El sistema muestra: "El huésped ya existe en el sistema". |
| CA-11 | HU-06 | Editar datos personales o de contacto de un huésped registrado con datos válidos. | El sistema actualiza sus datos. |
| CA-12 | HU-06 | Actualizar un huésped que no está registrado. | El sistema informa que el huésped no fue encontrado y no realiza cambios. |
| CA-13 | HU-07 | Eliminar y confirmar un huésped sin hospedaje activo, reservas vigentes ni facturas sin pagar. | El sistema elimina el registro del huésped. |
| CA-14 | HU-07 | Eliminar un huésped con hospedaje activo, reserva vigente o factura sin pagar. | El sistema restringe la eliminación. |
| CA-15 | HU-08 | Buscar por nombre, apellido o documento con huéspedes coincidentes. | El sistema muestra los resultados coincidentes. |
| CA-16 | HU-08 | Buscar sin resultados coincidentes. | El sistema informa que no se encontraron resultados. |
| CA-17 | HU-09 | Crear una reserva con una habitación disponible en todo el rango y huésped registrado. | El sistema registra la reserva en estado Pendiente y asigna la habitación para ese periodo. |
| CA-18 | HU-09 | Confirmar una reserva con cruce de fechas con otra reserva. | El sistema muestra: "La habitación no está disponible en el rango de fechas seleccionado". |
| CA-19 | HU-10 | Cancelar y confirmar una reserva pendiente. | El sistema anula la reserva y libera esas fechas. |
| CA-20 | HU-10 | Cancelar una reserva que no existe o que ya está cancelada. | El sistema informa que no puede realizar la cancelación. |
| CA-21 | HU-11 | Consultar disponibilidad para un rango de fechas con habitaciones y reservas registradas. | El sistema muestra solo las habitaciones sin cruce de fechas. |
| CA-22 | HU-11 | Consultar un rango en el que una habitación tiene cruce con una reserva. | El sistema excluye esa habitación de las disponibles. |
| CA-23 | HU-12 | Registrar el check-in de una reserva válida con habitación asignada. | El sistema registra la llegada y cambia la habitación a Ocupada. |
| CA-24 | HU-12 | Registrar el check-in sin reserva válida o sin habitación disponible. | El sistema impide el ingreso. |
| CA-25 | HU-13 | Registrar el check-out de una estadía activa con la cuenta saldada. | El sistema registra la salida y cambia la habitación a Mantenimiento/Limpieza. |
| CA-26 | HU-13 | Registrar el check-out con saldo pendiente. | El sistema impide la salida hasta que la cuenta esté saldada. |
| CA-27 | HU-14 | Generar la factura de una estadía con noches, tarifa y servicios registrados. | El sistema calcula el total (noches × tarifa + cargo extra + servicios) y genera un comprobante detallado. |
| CA-28 | HU-14 | Generar la factura sin una estadía activa con datos suficientes. | El sistema impide la generación hasta completar la información requerida. |

---

## Anexo B. Matriz de trazabilidad

| HU | Descripción | RF | RN | CA | CU |
| :--- | :--- | :--- | :--- | :--- | :--- |
| HU-01 | Registro de habitaciones | RF-01 | RN-01 | CA-01, CA-02 | CU-01 |
| HU-02 | Modificación de habitaciones | RF-02 | RN-14 | CA-03, CA-04 | CU-01 |
| HU-03 | Eliminación de habitaciones | RF-03 | RN-02 | CA-05, CA-06 | CU-01 |
| HU-04 | Listado de habitaciones | RF-04 | — | CA-07, CA-08 | CU-01 |
| HU-05 | Registro de huéspedes | RF-05 | RN-03 | CA-09, CA-10 | — |
| HU-06 | Actualización de datos de huésped | RF-06 | — | CA-11, CA-12 | — |
| HU-07 | Eliminación de huésped | RF-07 | RN-04 | CA-13, CA-14 | — |
| HU-08 | Consulta de huéspedes | RF-08 | — | CA-15, CA-16 | — |
| HU-09 | Creación de reserva | RF-09 | RN-05, RN-13 | CA-17, CA-18 | CU-02 |
| HU-10 | Cancelación de reserva | RF-10 | RN-06 | CA-19, CA-20 | — |
| HU-11 | Consulta de disponibilidad | RF-11 | RN-07 | CA-21, CA-22 | CU-02 |
| HU-12 | Registro de check-in | RF-12 | RN-08, RN-15 | CA-23, CA-24 | CU-03 |
| HU-13 | Registro de check-out | RF-13 | RN-09, RN-10, RN-15 | CA-25, CA-26 | CU-03 |
| HU-14 | Generación de factura | RF-14 | RN-11, RN-12 | CA-27, CA-28 | CU-03 |

La relación de cada requisito con las clases Java y las tablas de la base de datos está en `docs/MATRIZ_TRAZABILIDAD.md`.

---

## Anexo C. Riesgos y limitaciones conocidas

| ID | Riesgo | Estado en la v2.0 |
| :--- | :--- | :--- |
| **R-01** | Pérdida de datos al cerrar la aplicación, porque no hay persistencia (RS-02). | **Mitigado parcialmente:** el modelo relacional de la Fase 4 está diseñado y probado; falta conectar la aplicación (JDBC o un framework de persistencia) en una fase futura. |
| **R-02** | Sin control de acceso: cualquier persona que ejecute el programa actúa como recepcionista. | **Abierto en la CLI.** El prototipo de la Fase 5 demuestra un control de acceso por roles (Administrador / Recepcionista), pero solo como propuesta: sus credenciales están dentro de la página y no son seguras. |
| **R-03** | Sin registro de auditoría de quién realiza cada operación. | **Abierto.** La base de datos guarda fechas de creación, emisión y pago, pero no el usuario. |
| **R-04** | Concurrencia no soportada: un único usuario por sesión. | **Abierto.** Deberá revisarse al conectar la base de datos. |
| **R-05** | Formato de validación de contacto no definido. | **Cerrado:** correo con formato válido (máx. 80 caracteres) y teléfono de 10 dígitos (ver 3.5). |

---

## Anexo D. Fichas de casos de uso (formato IEEE 830)

### Caso de uso 1: Gestionar habitaciones

| Campo | Contenido |
| :--- | :--- |
| **ID** | CU-01 (RF-01, RF-02, RF-03, RF-04) |
| **Nombre** | Gestionar habitaciones (registrar, modificar, eliminar, listar) |
| **Actores** | Recepcionista (principal). |
| **Precondiciones** | 1. El sistema está en ejecución y muestra el menú principal. 2. Para modificar o eliminar, la habitación existe. |
| **Flujo normal** | 1. El recepcionista elige *Habitaciones*. 2. El sistema muestra el submenú numerado con la opción *Volver*. 3. El recepcionista elige *Registrar* e ingresa número, tipo, capacidad, tarifa y, si es Suite, cargo extra. 4. El sistema valida los datos y que el número no exista, guarda la habitación como Disponible y confirma. |
| **Flujos alternativos** | A1. *Modificar*: el recepcionista ingresa el número y los nuevos valores; el sistema actualiza la habitación. A2. *Listar*: el sistema muestra número, tipo, capacidad, tarifa y estado de cada habitación, o filtra por estado. A3. *Cambiar estado*: el recepcionista habilita como Disponible una habitación en Mantenimiento/Limpieza, o la envía a mantenimiento. A4. *Volver*: el sistema regresa al menú principal sin cambios. |
| **Flujos anormales / excepciones** | E1. Número ya registrado (RN-01): "El número de habitación ya se encuentra registrado" (CA-02). E2. Número inexistente al modificar: la habitación no fue encontrada (CA-04). E3. Eliminar una habitación ocupada o con reservas futuras (RN-02): el sistema bloquea la acción (CA-06). E4. Cambiar el tipo con reservas pendientes o activas (RN-14): el sistema lo rechaza. E5. Listado vacío: el sistema informa que no hay habitaciones (CA-08). |
| **Postcondiciones** | Éxito: la habitación queda registrada, modificada o eliminada. Excepción: el estado del sistema no cambia. |

### Caso de uso 2: Crear reserva

| Campo | Contenido |
| :--- | :--- |
| **ID** | CU-02 (RF-09, RF-11) |
| **Nombre** | Crear reserva |
| **Actores** | Recepcionista (ejecuta); Huésped (sujeto de datos, origen de la solicitud). |
| **Precondiciones** | 1. El huésped está registrado (RF-05). 2. Existe al menos una habitación registrada. |
| **Flujo normal** | 1. El recepcionista elige *Crear reserva*. 2. El sistema solicita documento del huésped, número de habitación y fechas de entrada y salida. 3. El recepcionista ingresa los datos. 4. El sistema valida las fechas, que la habitación no esté en mantenimiento y que no haya cruce (RN-05, RN-13). 5. El sistema registra la reserva como Pendiente con código `RES-AAAA-NNN` y la muestra (CA-17). |
| **Flujos alternativos** | A1. El recepcionista consulta primero la disponibilidad del rango (RF-11) y elige una habitación libre. A2. El recepcionista vuelve al menú antes de confirmar: no se registra la reserva. |
| **Flujos anormales / excepciones** | E1. Salida igual o anterior a la entrada, o entrada anterior a hoy: el sistema rechaza la reserva. E2. Cruce de fechas (RN-05): "La habitación no está disponible en el rango de fechas seleccionado" (CA-18). E3. Habitación en mantenimiento (RN-13): el sistema rechaza la reserva. E4. Documento no registrado: el sistema informa que el huésped no fue encontrado. |
| **Postcondiciones** | Éxito: la reserva queda creada y bloquea la habitación para esas fechas. Excepción: no se crea la reserva ni se consume un código. |

### Caso de uso 3: Registrar estadía

| Campo | Contenido |
| :--- | :--- |
| **ID** | CU-03 (RF-12, RF-13, RF-14) |
| **Nombre** | Registrar estadía (check-in, servicios, facturación, pago y check-out) |
| **Actores** | Recepcionista (ejecuta); Huésped (sujeto de datos). |
| **Precondiciones** | 1. Para el check-in, existe una reserva pendiente dentro de su periodo y la habitación está Disponible. 2. Para facturar y para el check-out, la reserva está Activa. |
| **Flujo normal** | 1. El recepcionista elige *Check-in* e ingresa el código de la reserva. 2. El sistema valida la reserva, la pasa a Activa y la habitación a Ocupada (CA-23). 3. Durante la estadía, el recepcionista registra los servicios adicionales consumidos. 4. El recepcionista genera la factura; el sistema calcula el total (RN-11) y muestra el comprobante detallado (CA-27). 5. El recepcionista registra el pago total y el medio de pago; la cuenta queda saldada. 6. El recepcionista elige *Check-out*; el sistema verifica la cuenta saldada (RN-09), pasa la reserva a Completada y la habitación a Mantenimiento/Limpieza (RN-10, CA-25). |
| **Flujos alternativos** | A1. El recepcionista consulta la cuenta de la estadía (servicios, factura y estado de pago) antes del check-out. A2. Después de la limpieza, el recepcionista habilita la habitación como Disponible (CU-01, A3). |
| **Flujos anormales / excepciones** | E1. Check-in sin reserva válida, fuera de su periodo o con la habitación no disponible: el sistema impide el ingreso (CA-24). E2. Check-out sin factura o con la factura sin pagar (RN-09): el sistema impide la salida e informa el saldo (CA-26). E3. Generar factura sin estadía activa: el sistema la impide (CA-28). E4. Agregar servicios o generar otra factura después de facturar (RN-12): el sistema lo rechaza. |
| **Postcondiciones** | Éxito: la estadía queda facturada y pagada, la reserva Completada y la habitación en Mantenimiento/Limpieza. Excepción: la reserva y la habitación conservan su estado anterior y no se emite factura. |

---

## 4. Control de cambios

| Versión | Cambio | Motivo |
| :--- | :--- | :--- |
| 1.0 | Versión inicial en PDF: 14 RF, RN-01 a RN-11, 28 CA, Anexos A a D. | Entrega de la Fase 2. |
| 2.0 | Se mantienen los 14 RF y los 28 CA; se precisan valores y límites de cada dato (3.5) y los estados del sistema (1.3). | Alinear el SRS con la base de datos (Fase 4) y el código (Fase 6). |
| 2.0 | RF-13 y RF-14: la factura se emite durante la estadía, se registra el pago y luego se permite el check-out. | La v1.0 exigía la cuenta saldada (RN-09) pero no definía cuándo se factura y se paga. |
| 2.0 | RN-11 incluye el cargo extra fijo de la Suite. | Lo modelan el diagrama de clases (`SuiteRoom`) y la tabla `rooms`. |
| 2.0 | Nuevas RN-12 a RN-15 (factura inmutable, no reservar en el pasado ni en mantenimiento, cambio de tipo, estado Ocupada). | Reglas implementadas en la base de datos y el código que no estaban escritas. |
| 2.0 | RF-03 y RF-07: eliminación lógica. | Conservar el historial de reservas y facturas (claves foráneas con `RESTRICT`). |
| 2.0 | RS-02 menciona el modelo relacional de la Fase 4; RS-03 admite comentarios y mensajes en español; RNF06 pasa de Java 8 a Java 17. | Reflejar lo construido en las Fases 4 y 6. |
| 2.0 | Anexo B corregido (la tabla de la v1.0 tenía filas desalineadas) y Anexo C con el estado de cada riesgo. | Trazabilidad verificable. |
