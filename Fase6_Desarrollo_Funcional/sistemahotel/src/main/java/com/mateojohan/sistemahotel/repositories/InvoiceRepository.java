package com.mateojohan.sistemahotel.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mateojohan.sistemahotel.models.Guest;
import com.mateojohan.sistemahotel.models.Invoice;
import com.mateojohan.sistemahotel.models.Reservation;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByReservation(Reservation reservation);

    /** RN-12: una reserva tiene como máximo una factura. */
    boolean existsByReservation(Reservation reservation);

    List<Invoice> findAllByOrderByIdDesc();

    /** RN-04: el huésped tiene facturas sin pagar. */
    @Query("""
            SELECT COUNT(i) > 0 FROM Invoice i
             WHERE i.reservation.guest = :guest
               AND NOT EXISTS (SELECT p FROM Payment p WHERE p.invoice = i)
            """)
    boolean existsUnpaidByGuest(@Param("guest") Guest guest);
}
