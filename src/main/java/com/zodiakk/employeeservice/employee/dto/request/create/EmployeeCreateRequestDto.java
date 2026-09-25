package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class EmployeeCreateRequestDto {

    @Schema(description = "User identifier from Auth Service",
            example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074")
    @NotNull(message = ValidationMessages.USER_ID_REQUIRED)
    private UUID userId;

    @Schema(description = "Employee first name", example = "Ivan")
    @NotNull(message = ValidationMessages.FIRST_NAME_REQUIRED)
    @Size(min = 2, max = 100, message = ValidationMessages.FIRST_NAME_SIZE)
    private String firstName;

    @Schema(description = "Employee last name", example = "Petrov")
    @NotNull(message = ValidationMessages.LAST_NAME_REQUIRED)
    @Size(min = 2, max = 100, message = ValidationMessages.LAST_NAME_SIZE)
    private String lastName;

    @Schema(description = "Employee middle name", example = "Ivanovich")
    @Size(max = 100, message = ValidationMessages.MIDDLE_NAME_SIZE)
    private String middleName;

    @Schema(description = "Employee birth date", example = "1995-05-15")
    @Past(message = ValidationMessages.BIRTH_DATE_PAST)
    private LocalDate birthDate;

    @Schema(description = "Employee hire date", example = "2024-08-01")
    @NotNull(message = ValidationMessages.HIRE_DATE_REQUIRED)
    private LocalDate hireDate;

    @Schema(description = "Position identifier",
            example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074"
    )
    @NotNull(message = ValidationMessages.POSITION_REQUIRED)
    private UUID positionId;

    @Schema(description = "Specialization identifier",
            example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074")
    private UUID specializationId;

    @Schema(description = "Grade identifier",
            example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074"
    )
    @NotNull(message = ValidationMessages.GRADE_REQUIRED)
    private UUID gradeId;

    @Schema(description = "Department identifier",
            example = "e73dcc73-e1db-4c3a-9246-0e1c2de79074"
    )
    private UUID departmentId;

    @Schema(description = "Maximum number of direct subordinates", example = "8")
    @Min(value = 0, message = ValidationMessages.MAX_SUBORDINATES_MIN)
    @Max(value = 1000, message = ValidationMessages.MAX_SUBORDINATES_MAX)
    private Integer maxSubordinates;

    @Schema(description = "Employment status", example = "ACTIVE")
    @NotNull(message = ValidationMessages.EMPLOYMENT_STATUS_REQUIRED)
    private EmploymentStatus employmentStatus;
}