package com.zodiakk.employeeservice.employee.exception.grade;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class GradeInUseException extends BusinessException {

    public GradeInUseException(UUID id) {
        super(ErrorMessages.GRADE_IN_USE.formatted(id));
    }
}
