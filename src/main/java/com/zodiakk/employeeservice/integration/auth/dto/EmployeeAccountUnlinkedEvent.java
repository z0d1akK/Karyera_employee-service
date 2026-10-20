package com.zodiakk.employeeservice.integration.auth.dto;

import java.util.UUID;

public record EmployeeAccountUnlinkedEvent(
        UUID userId,
        UUID employeeId
) {
}