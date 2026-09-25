package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.ResponsibilityTypeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.ResponsibilityTypeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ResponsibilityTypeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ResponsibilityTypeResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ResponsibilityTypeService {

    ResponsibilityTypeResponseDto create(ResponsibilityTypeCreateRequestDto request);

    ResponsibilityTypeResponseDto getById(UUID id);

    Page<ResponsibilityTypeResponseDto> findAll(ResponsibilityTypeFilterDto filter, PageRequestDto pageRequest);

    ResponsibilityTypeResponseDto update(UUID id, ResponsibilityTypeUpdateRequestDto request);

    void deactivate(UUID id);

    void activate(UUID id);

    void delete(UUID id);
}
