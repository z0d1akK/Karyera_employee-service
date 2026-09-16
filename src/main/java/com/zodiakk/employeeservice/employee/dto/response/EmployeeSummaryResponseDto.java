package com.zodiakk.employeeservice.employee.dto.response;

import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSummaryResponseDto {

    @Schema(description = "Employee identifier")
    private UUID id;

    @Schema(description = "Employee user identifier")
    private UUID userId;

    @Schema(description = "Employee firstname")
    private String firstName;

    @Schema(description = "Employee lastname")
    private String lastName;

    @Schema(description = "Employee middlename")
    private String middleName;

    @Schema(description = "Employee position")
    private PositionResponseDto position;

    @Schema(description = "Employee specialization")
    private SpecializationResponseDto specialization;

    @Schema(description = "Employee grade")
    private GradeResponseDto grade;

    @Schema(description = "Employee employment status")
    private EmploymentStatus employmentStatus;
}