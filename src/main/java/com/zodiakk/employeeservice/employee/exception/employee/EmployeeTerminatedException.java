package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class EmployeeTerminatedException extends BusinessException {

    public EmployeeTerminatedException(UUID id) {
        super(ErrorMessages.EMPLOYEE_TERMINATED.formatted(id));
    }
}
