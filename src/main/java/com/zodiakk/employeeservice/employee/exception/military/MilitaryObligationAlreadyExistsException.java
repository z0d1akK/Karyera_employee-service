package com.zodiakk.employeeservice.employee.exception.military;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class MilitaryObligationAlreadyExistsException extends BusinessException {

    public MilitaryObligationAlreadyExistsException(UUID employeeId) {
        super(ErrorMessages.MILITARY_OBLIGATION_ALREADY_EXISTS.formatted(employeeId));
    }
}
