package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Identifiers of employees in the requested employment status")
public record ActiveEmployeeIdsResponseDto(
        List<UUID> employeeIds
) {
}
