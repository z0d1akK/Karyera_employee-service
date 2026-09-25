package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.PositionCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.PositionFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.PositionUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.PositionResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PositionService {

    PositionResponseDto create(PositionCreateRequestDto request);

    PositionResponseDto getById(UUID id);

    Page<PositionResponseDto> findAll(PositionFilterDto filter, PageRequestDto pageRequest);

    PositionResponseDto update(UUID id, PositionUpdateRequestDto request);

    void deactivate(UUID id);

    void activate(UUID id);

    void delete(UUID id);
}
