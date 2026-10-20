package com.zodiakk.employeeservice.integration.auth.dto;

import java.util.UUID;

public record UserDeletedEvent(
        UUID userId,
        UUID employeeId
) {
}