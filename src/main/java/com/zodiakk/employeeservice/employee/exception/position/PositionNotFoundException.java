package com.zodiakk.employeeservice.employee.exception.position;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class PositionNotFoundException extends ResourceNotFoundException {

    public PositionNotFoundException(UUID id) {
        super(ErrorMessages.POSITION_NOT_FOUND.formatted(id));
    }
}