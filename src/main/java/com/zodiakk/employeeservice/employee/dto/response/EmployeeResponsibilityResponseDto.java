package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponsibilityResponseDto {

    @Schema(description = "Employee responsibility identifier")
    private UUID id;

    @Schema(description = "Employee responsibility employee identifier")
    private UUID employeeId;

    @Schema(description = "Employee responsibility employee summary")
    private EmployeeSummaryResponseDto responsibleEmployee;

    @Schema(description = "Employee responsibility type")
    private ResponsibilityTypeResponseDto responsibilityType;

    @Schema(description = "Employee responsibility start date")
    private LocalDate startDate;

    @Schema(description = "Employee responsibility end date")
    private LocalDate endDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}