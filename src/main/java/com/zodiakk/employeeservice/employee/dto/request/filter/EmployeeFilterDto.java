package com.zodiakk.employeeservice.employee.dto.request.filter;

import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFilterDto {

    @Schema(description = "Part of first name, case-insensitive", example = "Ivan")
    private String firstName;

    @Schema(description = "Part of last name, case-insensitive", example = "Petrov")
    private String lastName;

    @Schema(description = "Department identifier")
    private UUID departmentId;

    @Schema(description = "Position identifier")
    private UUID positionId;

    @Schema(description = "Grade identifier")
    private UUID gradeId;

    @Schema(description = "Specialization identifier")
    private UUID specializationId;

    @Schema(description = "Employment status", example = "ACTIVE")
    private EmploymentStatus employmentStatus;

    @Schema(description = "Hire date from (inclusive)")
    private LocalDate hireDateFrom;

    @Schema(description = "Hire date to (inclusive)")
    private LocalDate hireDateTo;
}
