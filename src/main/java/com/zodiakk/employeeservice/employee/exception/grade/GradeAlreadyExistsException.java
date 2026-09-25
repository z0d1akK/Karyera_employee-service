package com.zodiakk.employeeservice.employee.exception.grade;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class GradeAlreadyExistsException extends BusinessException {

    public GradeAlreadyExistsException() {
        super(ErrorMessages.GRADE_ALREADY_EXISTS);
    }
}
