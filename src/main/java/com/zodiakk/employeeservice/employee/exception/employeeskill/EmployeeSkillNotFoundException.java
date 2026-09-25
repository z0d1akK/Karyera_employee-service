package com.zodiakk.employeeservice.employee.exception.employeeskill;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class EmployeeSkillNotFoundException extends ResourceNotFoundException {

    public EmployeeSkillNotFoundException(UUID id) {
        super(ErrorMessages.EMPLOYEE_SKILL_NOT_FOUND.formatted(id));
    }
}
