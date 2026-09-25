package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class EmployeeCannotBeCreatedTerminatedException extends BusinessException {

    public EmployeeCannotBeCreatedTerminatedException() {
        super(ErrorMessages.EMPLOYEE_CANNOT_BE_CREATED_TERMINATED);
    }
}
