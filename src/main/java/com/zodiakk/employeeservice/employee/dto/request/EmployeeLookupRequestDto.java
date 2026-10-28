package com.zodiakk.employeeservice.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Batch lookup request for employee reference data")
public record EmployeeLookupRequestDto(

        @Schema(description = "Employee identifiers")
        @NotNull
        @Size(max = 200)
        List<UUID> employeeIds
) {
}
