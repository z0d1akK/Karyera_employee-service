package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.EmployeeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSummaryResponseDto;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface EmployeeService {

    EmployeeResponseDto create(EmployeeCreateRequestDto request);

    EmployeeResponseDto getById(UUID id);

    EmployeeResponseDto getByUserId(UUID userId);

    Page<EmployeeSummaryResponseDto> findAll(EmployeeFilterDto filter, PageRequestDto pageRequest);

    EmployeeResponseDto update(UUID id, EmployeeUpdateRequestDto request);

    EmployeeResponseDto changeStatus(UUID id, EmploymentStatus status);

    void delete(UUID id);
}
