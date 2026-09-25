package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.DepartmentCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.DepartmentFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.DepartmentResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface DepartmentService {

    DepartmentResponseDto create(DepartmentCreateRequestDto request);

    DepartmentResponseDto getById(UUID id);

    Page<DepartmentResponseDto> findAll(DepartmentFilterDto filter, PageRequestDto pageRequest);

    DepartmentResponseDto update(UUID id, DepartmentUpdateRequestDto request);

    void deactivate(UUID id);

    void activate(UUID id);

    void delete(UUID id);
}