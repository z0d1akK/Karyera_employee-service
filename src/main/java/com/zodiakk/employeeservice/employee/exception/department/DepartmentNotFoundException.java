package com.zodiakk.employeeservice.employee.exception.department;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class DepartmentNotFoundException extends ResourceNotFoundException {

    public DepartmentNotFoundException(UUID id) {
        super(ErrorMessages.DEPARTMENT_NOT_FOUND.formatted(id));
    }
}