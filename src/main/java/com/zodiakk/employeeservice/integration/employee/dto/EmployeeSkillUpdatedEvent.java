package com.zodiakk.employeeservice.integration.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeSkillUpdatedEvent(
        UUID employeeId,
        UUID skillId,
        Integer level,
        BigDecimal yearsOfExperience,
        LocalDate lastAssessedAt,
        String changeType
) {
}
