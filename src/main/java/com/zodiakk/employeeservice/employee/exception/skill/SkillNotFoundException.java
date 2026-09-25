package com.zodiakk.employeeservice.employee.exception.skill;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class SkillNotFoundException extends ResourceNotFoundException {

    public SkillNotFoundException(UUID id) {
        super(ErrorMessages.SKILL_NOT_FOUND.formatted(id));
    }
}
