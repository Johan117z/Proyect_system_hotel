package com.mateojohan.sistemahotel.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mateojohan.sistemahotel.dto.AdditionalServiceRequestDTO;
import com.mateojohan.sistemahotel.dto.ApiResponse;
import com.mateojohan.sistemahotel.dto.InvoiceResponseDTO;
import com.mateojohan.sistemahotel.dto.ReservationRequestDTO;
import com.mateojohan.sistemahotel.dto.ReservationResponseDTO;
import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.services.BillingService;
import com.mateojohan.sistemahotel.services.ReservationService;

import jakarta.validation.Valid;

/** Épicas 3 y 4: Reservas (HU-09, HU-10) y Estadía (HU-12, HU-13). */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final BillingService billingService;

    public ReservationController(ReservationService reservationService, BillingService billingService) {
        this.reservationService = reservationService;
        this.billingService = billingService;
    }

    /** GET /api/reservations o /api/reservations?status=PENDING */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> findAll(
            @RequestParam(required = false) ReservationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.findAll(status)));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.findByCode(code)));
    }

    /** HU-09 */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> create(@Valid @RequestBody ReservationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reservationService.create(request)));
    }

    /** HU-10 */
    @PatchMapping("/{code}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> cancel(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.cancel(code)));
    }

    /** HU-12 */
    @PatchMapping("/{code}/check-in")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> checkIn(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.checkIn(code)));
    }

    /** Servicio adicional durante la estadía */
    @PostMapping("/{code}/services")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> addService(
            @PathVariable String code, @Valid @RequestBody AdditionalServiceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(reservationService.addService(code, request)));
    }

    /** HU-14: emite la factura de la estadía */
    @PostMapping("/{code}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> generateInvoice(@PathVariable String code) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(billingService.generateInvoice(code)));
    }

    /** Factura de la reserva, si ya se emitió */
    @GetMapping("/{code}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> findInvoice(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(billingService.findByReservation(code)));
    }

    /** HU-13: exige la cuenta saldada (RN-09) */
    @PatchMapping("/{code}/check-out")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> checkOut(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.checkOut(code)));
    }
}
