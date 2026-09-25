package com.zodiakk.employeeservice.employee.exception.employeeresponsibility;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class SelfResponsibilityException extends BusinessException {

    public SelfResponsibilityException(UUID employeeId) {
        super(ErrorMessages.SELF_RESPONSIBILITY.formatted(employeeId));
    }
}
