package com.mateojohan.sistemahotel.services;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Genera el código RES-AAAA-NNN a partir de la tabla code_sequences.
 * <p>
 * La fila se lee con SELECT ... FOR UPDATE, así que queda bloqueada hasta que termina la transacción
 * que crea la reserva. Con eso:
 * <ul>
 *   <li>dos reservas simultáneas nunca reciben el mismo código (antes se usaba count() + 1);</li>
 *   <li>las reservas se crean de una en una, de modo que el control de cruce de fechas (RN-05)
 *       no lo pueden pasar dos peticiones al mismo tiempo.</li>
 * </ul>
 * Si la reserva se rechaza, la transacción se deshace y el número no se pierde.
 */
@Component
public class ReservationCodeGenerator {

    private static final String SEQUENCE = "reservation";

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Debe ser la primera consulta de la transacción: en InnoDB la "foto" de los datos se toma en la
     * primera lectura normal, y si esa lectura ocurriera antes del bloqueo, el control de cruce
     * podría no ver una reserva que otra petición acaba de guardar.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String next(LocalDate checkInDate) {
        Number value = (Number) entityManager
                .createNativeQuery("SELECT next_value FROM code_sequences WHERE name = :name FOR UPDATE")
                .setParameter("name", SEQUENCE)
                .getSingleResult();
        entityManager
                .createNativeQuery("UPDATE code_sequences SET next_value = next_value + 1 WHERE name = :name")
                .setParameter("name", SEQUENCE)
                .executeUpdate();
        return String.format("RES-%d-%03d", checkInDate.getYear(), value.longValue());
    }
}
