package com.crm.freelance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String password
) {
}
