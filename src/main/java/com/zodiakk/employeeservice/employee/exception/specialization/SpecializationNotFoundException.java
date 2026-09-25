package com.zodiakk.employeeservice.employee.exception.specialization;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class SpecializationNotFoundException extends ResourceNotFoundException {

    public SpecializationNotFoundException(UUID id) {
        super(ErrorMessages.SPECIALIZATION_NOT_FOUND.formatted(id));
    }
}