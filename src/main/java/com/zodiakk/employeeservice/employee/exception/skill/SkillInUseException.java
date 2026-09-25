package com.zodiakk.employeeservice.employee.exception.skill;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class SkillInUseException extends BusinessException {

    public SkillInUseException(UUID id) {
        super(ErrorMessages.SKILL_IN_USE.formatted(id));
    }
}
