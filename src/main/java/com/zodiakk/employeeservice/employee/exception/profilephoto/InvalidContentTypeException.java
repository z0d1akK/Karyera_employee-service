package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class InvalidContentTypeException extends BusinessException {

    public InvalidContentTypeException(String contentType) {
        super(ErrorMessages.INVALID_CONTENT_TYPE.formatted(contentType));
    }
}
