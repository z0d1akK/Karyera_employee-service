package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.SpecializationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SpecializationFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SpecializationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SpecializationResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SpecializationService {

    SpecializationResponseDto create(SpecializationCreateRequestDto request);

    SpecializationResponseDto getById(UUID id);

    Page<SpecializationResponseDto> findAll(SpecializationFilterDto filter, PageRequestDto pageRequest);

    SpecializationResponseDto update(UUID id, SpecializationUpdateRequestDto request);

    void deactivate(UUID id);

    void activate(UUID id);

    void delete(UUID id);
}
