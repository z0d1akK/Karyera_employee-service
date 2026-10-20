package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class EmptyFileException extends BusinessException {

    public EmptyFileException() {
        super(ErrorMessages.EMPTY_FILE);
    }
}
