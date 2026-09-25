package com.zodiakk.employeeservice.employee.exception.specialization;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class SpecializationAlreadyExistsException extends BusinessException {

    public SpecializationAlreadyExistsException(String name) {
        super(ErrorMessages.SPECIALIZATION_ALREADY_EXISTS.formatted(name));
    }
}