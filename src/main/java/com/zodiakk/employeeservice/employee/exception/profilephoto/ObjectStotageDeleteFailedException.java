package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class ObjectStotageDeleteFailedException extends BusinessException {

    public ObjectStotageDeleteFailedException(Throwable cause) {
        super(ErrorMessages.OBJECT_STORAGE_DELETE_FAILED);
        initCause(cause);
    }
}
