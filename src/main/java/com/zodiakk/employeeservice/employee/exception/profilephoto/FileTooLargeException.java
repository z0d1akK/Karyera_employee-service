package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class FileTooLargeException extends BusinessException {

    public FileTooLargeException(long maxSize) {
        super(ErrorMessages.FILE_TOO_LARGE.formatted(maxSize));
    }
}
