package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateRequestDto {

    @Schema(description = "Employee first name", example = "Ivan")
    @Size(
            min = 2,
            max = 100,
            message = ValidationMessages.FIRST_NAME_SIZE
    )
    private String firstName;

    @Schema(description = "Employee last name", example = "Petrov")
    @Size(
            min = 2,
            max = 100,
            message = ValidationMessages.LAST_NAME_SIZE
    )
    private String lastName;

    @Schema(description = "Employee middle name", example = "Ivanovich")
    @Size(
            max = 100,
            message = ValidationMessages.MIDDLE_NAME_SIZE
    )
    private String middleName;

    @Schema(description = "Employee birth date", example = "1995-05-15")
    @Past(message = ValidationMessages.BIRTH_DATE_PAST)
    private LocalDate birthDate;

    @Schema(description = "Employee hire date", example = "2024-08-01")
    private LocalDate hireDate;

    @Schema(description = "Position identifier")
    private UUID positionId;

    @Schema(description = "Specialization identifier")
    private UUID specializationId;

    @Schema(description = "Grade identifier")
    private UUID gradeId;

    @Schema(description = "Department identifier")
    private UUID departmentId;

    @Schema(description = "Maximum number of direct subordinates")
    @Min(value = 0, message = ValidationMessages.MAX_SUBORDINATES_MIN)
    @Max(value = 1000, message = "Maximum number of subordinates cannot exceed {value}")
    private Integer maxSubordinates;

    @Schema(description = "Employment status", example = "ACTIVE")
    private EmploymentStatus employmentStatus;
}