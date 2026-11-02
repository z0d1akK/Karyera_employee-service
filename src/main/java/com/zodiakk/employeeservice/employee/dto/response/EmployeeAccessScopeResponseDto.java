package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Employee access scope resolved for an acting employee")
public record EmployeeAccessScopeResponseDto(

        @Schema(description = "Target employee identifier")
        UUID employeeId,

        @Schema(description = "Acting employee identifier")
        UUID actorEmployeeId,

        @Schema(description = "Whether the actor is the target employee")
        boolean self,

        @Schema(description = "Whether the actor is the target employee's manager")
        boolean managerOfEmployee,

        @Schema(description = "Whether the target employee is active")
        boolean active
) {
}
