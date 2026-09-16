package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkillResponseDto {

    @Schema(description = "Employee skill identifier")
    private UUID id;

    @Schema(description = "Employee skill employee identifier")
    private UUID employeeId;

    @Schema(description = "Employee skill skill")
    private SkillResponseDto skill;

    @Schema(description = "Employee skill level")
    private Integer level;

    @Schema(description = "Employee skill years of experience")
    private BigDecimal yearsOfExperience;

    @Schema(description = "Employee skill last assessed date")
    private LocalDate lastAssessedAt;
}