package com.mateojohan.sistemahotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** HU-05: datos para registrar un huésped. Mismas reglas que los CHECK de la tabla guests. */
public record GuestRequestDTO(

        @NotBlank(message = "El documento es obligatorio.")
        @Pattern(regexp = "^[0-9]{6,10}$", message = "El documento debe tener entre 6 y 10 dígitos.")
        String document,

        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(max = 60, message = "El nombre admite máximo 60 caracteres.")
        String fullName,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido (ejemplo@correo.com).")
        @Size(max = 80, message = "El correo admite máximo 80 caracteres.")
        String email,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos.")
        String phone) {
}
