package com.mateojohan.sistemahotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mateojohan.sistemahotel.models.AdditionalService;

public record AdditionalServiceResponseDTO(
        Long id,
        String description,
        BigDecimal amount,
        LocalDateTime registeredAt) {

    public static AdditionalServiceResponseDTO from(AdditionalService service) {
        return new AdditionalServiceResponseDTO(service.getId(), service.getDescription(),
                service.getAmount(), service.getRegisteredAt());
    }
}
