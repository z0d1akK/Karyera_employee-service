package com.zodiakk.employeeservice.employee.exception.position;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class PositionInUseException extends BusinessException {

    public PositionInUseException(UUID id) {
        super(ErrorMessages.POSITION_IN_USE.formatted(id));
    }
}
