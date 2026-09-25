package com.zodiakk.employeeservice.employee.exception.responsibilitytype;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class ResponsibilityTypeInUseException extends BusinessException {

    public ResponsibilityTypeInUseException(UUID id) {
        super(ErrorMessages.RESPONSIBILITY_TYPE_IN_USE.formatted(id));
    }
}
