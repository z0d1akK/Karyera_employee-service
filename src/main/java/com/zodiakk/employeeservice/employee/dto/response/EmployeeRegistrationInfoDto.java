
package com.zodiakk.employeeservice.employee.dto.response;

import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;

import java.util.UUID;

public record EmployeeRegistrationInfoDto(
        UUID employeeId,
        String email,
        EmploymentStatus employmentStatus,
        UUID userId
) {
}