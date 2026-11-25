package com.mateojohan.sistemahotel.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mateojohan.sistemahotel.models.Invoice;
import com.mateojohan.sistemahotel.models.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** RN-09: la factura ya está pagada (cuenta saldada). */
    boolean existsByInvoice(Invoice invoice);

    List<Payment> findAllByOrderByIdDesc();
}
