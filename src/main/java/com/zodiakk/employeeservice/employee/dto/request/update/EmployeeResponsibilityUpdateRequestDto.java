package com.zodiakk.employeeservice.employee.dto.request.update;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponsibilityUpdateRequestDto {

    private UUID responsibleEmployeeId;

    private UUID responsibilityTypeId;

    private LocalDate startDate;

    private LocalDate endDate;
}
