package com.crm.freelance.dto;

import com.crm.freelance.model.StatutRelance;

import java.time.LocalDateTime;

public record RelanceResponse(
        Long id,
        LocalDateTime date,
        String note,
        StatutRelance statut
) {
}
