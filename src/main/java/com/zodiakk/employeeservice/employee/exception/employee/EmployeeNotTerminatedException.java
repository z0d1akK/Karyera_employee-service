package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class EmployeeNotTerminatedException extends BusinessException {

    public EmployeeNotTerminatedException(UUID id) {
        super(ErrorMessages.EMPLOYEE_NOT_TERMINATED.formatted(id));
    }
}
