package com.zodiakk.employeeservice.employee.exception.military;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class MilitaryObligationNotFoundException extends ResourceNotFoundException {

    public MilitaryObligationNotFoundException(UUID employeeId) {
        super(ErrorMessages.MILITARY_OBLIGATION_NOT_FOUND.formatted(employeeId));
    }
}
