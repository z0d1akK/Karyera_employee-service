package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class EmployeeNotFoundException extends ResourceNotFoundException {

    public EmployeeNotFoundException(UUID id) {
        super(ErrorMessages.EMPLOYEE_NOT_FOUND.formatted(id));
    }

    public static EmployeeNotFoundException byUserId(UUID userId) {
        return new EmployeeNotFoundException(ErrorMessages.EMPLOYEE_NOT_FOUND_BY_USER_ID.formatted(userId));
    }

    private EmployeeNotFoundException(String message) {
        super(message);
    }
}
