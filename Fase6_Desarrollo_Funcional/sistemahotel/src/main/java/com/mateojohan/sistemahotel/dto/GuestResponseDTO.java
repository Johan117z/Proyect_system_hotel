package com.mateojohan.sistemahotel.dto;

import java.time.LocalDateTime;

import com.mateojohan.sistemahotel.models.Guest;

/** Huésped tal como lo recibe el cliente Web/Móvil. */
public record GuestResponseDTO(
        Integer id,
        String document,
        String fullName,
        String email,
        String phone,
        LocalDateTime createdAt) {

    public static GuestResponseDTO from(Guest guest) {
        return new GuestResponseDTO(guest.getId(), guest.getDocument(), guest.getFullName(),
                guest.getEmail(), guest.getPhone(), guest.getCreatedAt());
    }
}
