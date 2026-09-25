package com.zodiakk.employeeservice.employee.exception.grade;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class GradeNotFoundException extends ResourceNotFoundException {

    public GradeNotFoundException(UUID id) {
        super(ErrorMessages.GRADE_NOT_FOUND.formatted(id));
    }
}
