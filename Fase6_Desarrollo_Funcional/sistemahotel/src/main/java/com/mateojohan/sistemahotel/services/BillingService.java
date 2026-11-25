package com.mateojohan.sistemahotel.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mateojohan.sistemahotel.dto.InvoiceResponseDTO;
import com.mateojohan.sistemahotel.dto.PaymentRequestDTO;
import com.mateojohan.sistemahotel.dto.PaymentResponseDTO;
import com.mateojohan.sistemahotel.exceptions.BusinessRuleException;
import com.mateojohan.sistemahotel.exceptions.ResourceNotFoundException;
import com.mateojohan.sistemahotel.models.Invoice;
import com.mateojohan.sistemahotel.models.Payment;
import com.mateojohan.sistemahotel.models.Reservation;
import com.mateojohan.sistemahotel.repositories.InvoiceRepository;
import com.mateojohan.sistemahotel.repositories.PaymentRepository;

/** Épica 5: Facturación (HU-14) y pagos (RN-09). */
@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationService reservationService;

    public BillingService(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository,
                          ReservationService reservationService) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.reservationService = reservationService;
    }

    /** HU-14 / RN-11: factura de una estadía en curso. Una reserva tiene como máximo una factura (RN-12). */
    @Transactional
    public InvoiceResponseDTO generateInvoice(String reservationCode) {
        Reservation reservation = reservationService.getReservation(reservationCode);
        if (invoiceRepository.existsByReservation(reservation)) {
            throw new BusinessRuleException("RN-12",
                    "La estadía " + reservation.getReservationCode() + " ya fue facturada.");
        }
        Invoice invoice = invoiceRepository.save(Invoice.issue(reservation));
        return InvoiceResponseDTO.from(invoice, false);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> findAll() {
        return invoiceRepository.findAllByOrderByIdDesc().stream()
                .map(i -> InvoiceResponseDTO.from(i, paymentRepository.existsByInvoice(i)))
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO findById(Long id) {
        Invoice invoice = getInvoice(id);
        return InvoiceResponseDTO.from(invoice, paymentRepository.existsByInvoice(invoice));
    }

    /** Factura de una reserva, si ya se emitió. */
    @Transactional(readOnly = true)
    public InvoiceResponseDTO findByReservation(String reservationCode) {
        Reservation reservation = reservationService.getReservation(reservationCode);
        Invoice invoice = invoiceRepository.findByReservation(reservation)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La reserva " + reservation.getReservationCode() + " aún no tiene factura."));
        return InvoiceResponseDTO.from(invoice, paymentRepository.existsByInvoice(invoice));
    }

    /** RN-09: pago total de la factura; con esto la cuenta queda saldada. */
    @Transactional
    public PaymentResponseDTO registerPayment(Long invoiceId, PaymentRequestDTO request) {
        Invoice invoice = getInvoice(invoiceId);
        if (paymentRepository.existsByInvoice(invoice)) {
            throw new BusinessRuleException("RN-09", "La factura No. " + invoiceId + " ya está pagada.");
        }
        return PaymentResponseDTO.from(paymentRepository.save(new Payment(invoice, request.method())));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findAllPayments() {
        return paymentRepository.findAllByOrderByIdDesc().stream().map(PaymentResponseDTO::from).toList();
    }

    private Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la factura No. " + id + "."));
    }
}
