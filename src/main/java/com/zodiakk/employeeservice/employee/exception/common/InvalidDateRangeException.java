package com.zodiakk.employeeservice.employee.exception.common;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class InvalidDateRangeException extends BusinessException {

    public InvalidDateRangeException() {
        super(ErrorMessages.INVALID_DATE_RANGE);
    }
}
