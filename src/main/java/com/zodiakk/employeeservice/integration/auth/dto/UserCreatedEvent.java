package com.zodiakk.employeeservice.integration.auth.dto;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        UUID employeeId
) {
}