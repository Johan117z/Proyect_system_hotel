package com.mateojohan.sistemahotel.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.mateojohan.sistemahotel.dto.RoomRateDTO;
import com.mateojohan.sistemahotel.dto.RoomRequestDTO;
import com.mateojohan.sistemahotel.dto.RoomResponseDTO;
import com.mateojohan.sistemahotel.dto.RoomStatusDTO;
import com.mateojohan.sistemahotel.dto.RoomTypeChangeDTO;
import com.mateojohan.sistemahotel.enums.RoomStatus;
import com.mateojohan.sistemahotel.services.RoomService;

import jakarta.validation.Valid;

/** Épica 1: Habitaciones (HU-01 a HU-04) y disponibilidad (HU-11). */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /** HU-04: GET /api/rooms o /api/rooms?status=AVAILABLE */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> findAll(
            @RequestParam(required = false) RoomStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.findAll(status)));
    }

    /** HU-11: GET /api/rooms/available?checkIn=2026-12-01&checkOut=2026-12-05 */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.findAvailable(checkIn, checkOut)));
    }

    /** HU-02 */
    @GetMapping("/{roomNumber}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> findByNumber(@PathVariable Integer roomNumber) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.findByNumber(roomNumber)));
    }

    /** HU-01 */
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponseDTO>> create(@Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(roomService.create(request)));
    }

    /** HU-02: cambio de tarifa */
    @PatchMapping("/{roomNumber}/rate")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRate(@PathVariable Integer roomNumber,
                                                                  @Valid @RequestBody RoomRateDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.updateRate(roomNumber, request)));
    }

    /** HU-02: cambio de tipo y capacidad */
    @PutMapping("/{roomNumber}/type")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> changeType(@PathVariable Integer roomNumber,
                                                                  @Valid @RequestBody RoomTypeChangeDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.changeType(roomNumber, request)));
    }

    /** Cambio manual AVAILABLE <-> MAINTENANCE */
    @PatchMapping("/{roomNumber}/status")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> changeStatus(@PathVariable Integer roomNumber,
                                                                    @Valid @RequestBody RoomStatusDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.changeStatus(roomNumber, request)));
    }

    /** HU-03: baja lógica */
    @DeleteMapping("/{roomNumber}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer roomNumber) {
        roomService.delete(roomNumber);
        return ResponseEntity.ok(ApiResponse.ok("La habitación " + roomNumber + " fue dada de baja."));
    }
}
