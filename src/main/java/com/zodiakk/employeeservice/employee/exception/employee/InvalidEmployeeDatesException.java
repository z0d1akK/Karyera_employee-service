package com.zodiakk.employeeservice.employee.exception.employee;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class InvalidEmployeeDatesException extends BusinessException {

    public InvalidEmployeeDatesException() {
        super(ErrorMessages.INVALID_EMPLOYEE_DATES);
    }
}
