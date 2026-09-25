package com.zodiakk.employeeservice.employee.exception.experience;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ExperienceNotFoundException extends ResourceNotFoundException {

    public ExperienceNotFoundException(UUID id) {
        super(ErrorMessages.EXPERIENCE_NOT_FOUND.formatted(id));
    }
}
