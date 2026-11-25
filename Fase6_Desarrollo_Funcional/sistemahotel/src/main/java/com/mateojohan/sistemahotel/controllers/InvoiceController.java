package com.mateojohan.sistemahotel.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mateojohan.sistemahotel.dto.ApiResponse;
import com.mateojohan.sistemahotel.dto.InvoiceResponseDTO;
import com.mateojohan.sistemahotel.dto.PaymentRequestDTO;
import com.mateojohan.sistemahotel.dto.PaymentResponseDTO;
import com.mateojohan.sistemahotel.services.BillingService;

import jakarta.validation.Valid;

/** Épica 5: Facturación (HU-14) y pagos (RN-09). La factura se emite desde /api/reservations/{code}/invoice. */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(billingService.findAll()));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> findAllPayments() {
        return ResponseEntity.ok(ApiResponse.ok(billingService.findAllPayments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billingService.findById(id)));
    }

    /** RN-09: pago total de la factura */
    @PostMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> registerPayment(@PathVariable Long id,
                                                                          @Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(billingService.registerPayment(id, request)));
    }
}
