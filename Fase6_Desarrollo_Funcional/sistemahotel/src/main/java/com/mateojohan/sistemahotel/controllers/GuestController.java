package com.mateojohan.sistemahotel.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mateojohan.sistemahotel.dto.ApiResponse;
import com.mateojohan.sistemahotel.dto.GuestContactDTO;
import com.mateojohan.sistemahotel.dto.GuestRequestDTO;
import com.mateojohan.sistemahotel.dto.GuestResponseDTO;
import com.mateojohan.sistemahotel.dto.GuestUpdateDTO;
import com.mateojohan.sistemahotel.services.GuestService;

import jakarta.validation.Valid;

/** Épica 2: Huéspedes (HU-05 a HU-08). */
@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GuestResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(guestService.findAll()));
    }

    /** HU-08: GET /api/guests/search?q=perez */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GuestResponseDTO>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(guestService.search(q)));
    }

    /** HU-06 */
    @GetMapping("/{document}")
    public ResponseEntity<ApiResponse<GuestResponseDTO>> findByDocument(@PathVariable String document) {
        return ResponseEntity.ok(ApiResponse.ok(guestService.findByDocument(document)));
    }

    /** HU-05 */
    @PostMapping
    public ResponseEntity<ApiResponse<GuestResponseDTO>> create(@Valid @RequestBody GuestRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(guestService.create(request)));
    }

    /** HU-06: datos personales y de contacto */
    @PutMapping("/{document}")
    public ResponseEntity<ApiResponse<GuestResponseDTO>> update(@PathVariable String document,
                                                               @Valid @RequestBody GuestUpdateDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(guestService.update(document, request)));
    }

    /** HU-06: solo datos de contacto */
    @PatchMapping("/{document}/contact")
    public ResponseEntity<ApiResponse<GuestResponseDTO>> updateContact(@PathVariable String document,
                                                                      @Valid @RequestBody GuestContactDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(guestService.updateContact(document, request)));
    }

    /** HU-07: baja lógica */
    @DeleteMapping("/{document}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String document) {
        guestService.delete(document);
        return ResponseEntity.ok(ApiResponse.ok("El huésped con documento " + document.trim() + " fue dado de baja."));
    }
}
