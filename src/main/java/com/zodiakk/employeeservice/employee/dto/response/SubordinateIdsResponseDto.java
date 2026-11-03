package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Active subordinates of an employee")
public record SubordinateIdsResponseDto(

        @Schema(description = "Manager employee identifier")
        UUID employeeId,

        @Schema(description = "Employees who currently report to the manager")
        List<UUID> subordinateIds
) {
}
