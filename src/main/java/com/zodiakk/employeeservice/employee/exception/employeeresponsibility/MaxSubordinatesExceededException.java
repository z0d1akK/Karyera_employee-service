package com.zodiakk.employeeservice.employee.exception.employeeresponsibility;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class MaxSubordinatesExceededException extends BusinessException {

    public MaxSubordinatesExceededException(UUID responsibleEmployeeId) {
        super(ErrorMessages.MAX_SUBORDINATES_EXCEEDED.formatted(responsibleEmployeeId));
    }
}
