package com.zodiakk.employeeservice.employee.exception.employeeskill;

import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class EmployeeSkillAlreadyExistsException extends BusinessException {

    public EmployeeSkillAlreadyExistsException(UUID skillId) {
        super(ErrorMessages.EMPLOYEE_SKILL_ALREADY_EXISTS.formatted(skillId));
    }
}
