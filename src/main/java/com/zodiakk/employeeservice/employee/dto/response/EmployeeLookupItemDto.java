package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Employee reference data used by other services")
public record EmployeeLookupItemDto(

        @Schema(description = "Employee identifier")
        UUID id,

        @Schema(description = "Employee full name")
        String fullName,

        @Schema(description = "Whether the employee is active")
        boolean active,

        @Schema(description = "Position identifier")
        UUID positionId,

        @Schema(description = "Grade identifier")
        UUID gradeId
) {
}
