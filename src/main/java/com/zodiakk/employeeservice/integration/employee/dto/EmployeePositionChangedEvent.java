package com.zodiakk.employeeservice.integration.employee.dto;

import java.util.UUID;

public record EmployeePositionChangedEvent(
        UUID employeeId,
        UUID previousPositionId,
        UUID previousGradeId,
        UUID positionId,
        UUID gradeId
) {
}
