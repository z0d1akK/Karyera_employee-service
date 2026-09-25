package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class EmployeeAlreadyExistsException extends BusinessException {

    public EmployeeAlreadyExistsException(UUID userId) {
        super(ErrorMessages.EMPLOYEE_ALREADY_EXISTS.formatted(userId));
    }
}
