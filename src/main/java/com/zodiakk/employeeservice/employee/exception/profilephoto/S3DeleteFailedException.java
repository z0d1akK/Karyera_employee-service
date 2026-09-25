package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class S3DeleteFailedException extends BusinessException {

    public S3DeleteFailedException() {
        super(ErrorMessages.S3_DELETE_FAILED);
    }

    public S3DeleteFailedException(Throwable cause) {
        super(ErrorMessages.S3_DELETE_FAILED);
        initCause(cause);
    }
}
