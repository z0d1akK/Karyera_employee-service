package com.zodiakk.employeeservice.employee.exception.responsibilitytype;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ResponsibilityTypeNotFoundException extends ResourceNotFoundException {

    public ResponsibilityTypeNotFoundException(UUID id) {
        super(ErrorMessages.RESPONSIBILITY_TYPE_NOT_FOUND.formatted(id));
    }
}
