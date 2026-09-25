package com.zodiakk.employeeservice.employee.exception.specialization;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class SpecializationInUseException extends BusinessException {

    public SpecializationInUseException(UUID id) {
        super(ErrorMessages.SPECIALIZATION_IN_USE.formatted(id));
    }
}
