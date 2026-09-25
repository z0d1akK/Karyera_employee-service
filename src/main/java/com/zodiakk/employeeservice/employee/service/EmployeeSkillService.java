package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeSkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeSkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSkillResponseDto;

import java.util.List;
import java.util.UUID;

public interface EmployeeSkillService {

    EmployeeSkillResponseDto add(UUID employeeId, EmployeeSkillCreateRequestDto request);

    List<EmployeeSkillResponseDto> listByEmployee(UUID employeeId);

    EmployeeSkillResponseDto update(UUID employeeId, UUID employeeSkillId, EmployeeSkillUpdateRequestDto request);

    void remove(UUID employeeId, UUID skillId);
}
