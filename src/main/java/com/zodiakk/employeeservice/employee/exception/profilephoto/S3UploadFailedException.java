package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class S3UploadFailedException extends BusinessException {

    public S3UploadFailedException() {
        super(ErrorMessages.S3_UPLOAD_FAILED);
    }

    public S3UploadFailedException(Throwable cause) {
        super(ErrorMessages.S3_UPLOAD_FAILED);
        initCause(cause);
    }
}
