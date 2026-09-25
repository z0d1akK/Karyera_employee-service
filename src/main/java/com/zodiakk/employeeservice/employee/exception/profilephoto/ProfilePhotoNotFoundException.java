package com.zodiakk.employeeservice.employee.exception.profilephoto;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ProfilePhotoNotFoundException extends ResourceNotFoundException {

    public ProfilePhotoNotFoundException(UUID employeeId) {
        super(ErrorMessages.PROFILE_PHOTO_NOT_FOUND.formatted(employeeId));
    }
}
