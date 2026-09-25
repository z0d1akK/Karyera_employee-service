package com.zodiakk.employeeservice.employee.exception.employeeresponsibility;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class ResponsibilityAlreadyActiveException extends BusinessException {

    public ResponsibilityAlreadyActiveException() {
        super(ErrorMessages.RESPONSIBILITY_ALREADY_ACTIVE);
    }
}
