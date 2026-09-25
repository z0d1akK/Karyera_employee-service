package com.zodiakk.employeeservice.employee.exception.education;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class EducationNotFoundException extends ResourceNotFoundException {

    public EducationNotFoundException(UUID id) {
        super(ErrorMessages.EDUCATION_NOT_FOUND.formatted(id));
    }
}
