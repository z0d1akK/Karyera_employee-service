package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class EmployeeAlreadyExistsException extends BusinessException {

    public EmployeeAlreadyExistsException(String email) {
        super(ErrorMessages.EMPLOYEE_ALREADY_EXISTS_BY_EMAIL.formatted(email));
    }

    public EmployeeAlreadyExistsException(UUID id) {
        super(ErrorMessages.EMPLOYEE_ALREADY_EXISTS.formatted(id));
    }
}
