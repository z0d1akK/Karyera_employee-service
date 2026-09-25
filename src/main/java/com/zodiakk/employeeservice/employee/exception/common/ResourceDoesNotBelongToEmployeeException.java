package com.zodiakk.employeeservice.employee.exception.common;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class ResourceDoesNotBelongToEmployeeException extends BusinessException {

    public ResourceDoesNotBelongToEmployeeException(UUID employeeId) {
        super(ErrorMessages.RESOURCE_DOES_NOT_BELONG_TO_EMPLOYEE.formatted(employeeId));
    }
}
