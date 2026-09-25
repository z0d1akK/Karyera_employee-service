package com.zodiakk.employeeservice.employee.exception.position;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class PositionAlreadyExistsException extends BusinessException {

    public PositionAlreadyExistsException(String name) {
        super(ErrorMessages.POSITION_ALREADY_EXISTS.formatted(name));
    }
}