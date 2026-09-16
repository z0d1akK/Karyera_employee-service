package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponsibilityUpdateRequestDto {

    @NotNull(message = ValidationMessages.RESPONSIBLE_EMPLOYEE_REQUIRED)
    private UUID responsibleEmployeeId;

    @NotNull(message = ValidationMessages.RESPONSIBILITY_TYPE_REQUIRED)
    private UUID responsibilityTypeId;

    @NotNull(message = ValidationMessages.START_DATE_REQUIRED)
    private LocalDate startDate;

    private LocalDate endDate;
}