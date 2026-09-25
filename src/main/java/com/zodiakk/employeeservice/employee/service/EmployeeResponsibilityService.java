package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeResponsibilityCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeResponsibilityUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponsibilityResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmployeeResponsibilityService {

    EmployeeResponsibilityResponseDto assign(UUID employeeId, EmployeeResponsibilityCreateRequestDto request);

    List<EmployeeResponsibilityResponseDto> listByEmployee(UUID employeeId);

    List<EmployeeResponsibilityResponseDto> listByResponsible(UUID responsibleEmployeeId);

    EmployeeResponsibilityResponseDto close(UUID responsibilityId, LocalDate endDate);

    EmployeeResponsibilityResponseDto update(UUID responsibilityId, EmployeeResponsibilityUpdateRequestDto request);

    void delete(UUID responsibilityId);
}
