package com.zodiakk.employeeservice.employee.exception.responsibilitytype;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class ResponsibilityTypeAlreadyExistsException extends BusinessException {

    public ResponsibilityTypeAlreadyExistsException(String code) {
        super(ErrorMessages.RESPONSIBILITY_TYPE_ALREADY_EXISTS.formatted(code));
    }
}
