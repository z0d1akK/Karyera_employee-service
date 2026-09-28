package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class ObjectStorageUploadFailedException extends BusinessException {

    public ObjectStorageUploadFailedException(Throwable cause) {
        super(ErrorMessages.OBJECT_STORAGE_UPLOAD_FAILED);
        initCause(cause);
    }
}
