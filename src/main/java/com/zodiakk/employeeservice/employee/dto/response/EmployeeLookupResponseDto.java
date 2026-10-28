package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Batch employee lookup response")
public record EmployeeLookupResponseDto(

        @Schema(description = "Resolved employees")
        List<EmployeeLookupItemDto> employees
) {
}
