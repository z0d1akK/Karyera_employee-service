package com.zodiakk.employeeservice.employee.exception.department;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class DepartmentInUseException extends BusinessException {

    public DepartmentInUseException(UUID id) {
        super(ErrorMessages.DEPARTMENT_IN_USE.formatted(id));
    }
}
