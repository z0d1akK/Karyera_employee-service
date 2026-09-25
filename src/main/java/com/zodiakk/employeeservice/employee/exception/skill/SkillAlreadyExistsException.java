package com.zodiakk.employeeservice.employee.exception.skill;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

public class SkillAlreadyExistsException extends BusinessException {

    public SkillAlreadyExistsException(String name) {
        super(ErrorMessages.SKILL_ALREADY_EXISTS.formatted(name));
    }
}
