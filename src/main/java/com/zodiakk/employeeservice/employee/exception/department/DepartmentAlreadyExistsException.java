package com.zodiakk.employeeservice.employee.exception.department;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class DepartmentAlreadyExistsException extends BusinessException {

    public DepartmentAlreadyExistsException(String name) {
        super(ErrorMessages.DEPARTMENT_ALREADY_EXISTS.formatted(name));
    }
}