package com.mateojohan.sistemahotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** HU-06: actualización solo de los datos de contacto. */
public record GuestContactDTO(

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido (ejemplo@correo.com).")
        @Size(max = 80, message = "El correo admite máximo 80 caracteres.")
        String email,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos.")
        String phone) {
}
