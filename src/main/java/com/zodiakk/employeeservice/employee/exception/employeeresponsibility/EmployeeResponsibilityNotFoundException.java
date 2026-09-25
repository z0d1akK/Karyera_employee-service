package com.zodiakk.employeeservice.employee.exception.employeeresponsibility;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class EmployeeResponsibilityNotFoundException extends ResourceNotFoundException {

    public EmployeeResponsibilityNotFoundException(UUID id) {
        super(ErrorMessages.EMPLOYEE_RESPONSIBILITY_NOT_FOUND.formatted(id));
    }
}
