package com.zodiakk.employeeservice.employee.exception.employeeresponsibility;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class MultipleNotAllowedException extends BusinessException {

    public MultipleNotAllowedException(String typeCode, UUID responsibleEmployeeId) {
        super(ErrorMessages.MULTIPLE_NOT_ALLOWED.formatted(typeCode, responsibleEmployeeId));
    }
}
