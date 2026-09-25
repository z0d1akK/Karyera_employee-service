package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.GradeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.GradeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.GradeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.GradeResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface GradeService {

    GradeResponseDto create(GradeCreateRequestDto request);

    GradeResponseDto getById(UUID id);

    Page<GradeResponseDto> findAll(GradeFilterDto filter, PageRequestDto pageRequest);

    GradeResponseDto update(UUID id, GradeUpdateRequestDto request);

    void deactivate(UUID id);

    void activate(UUID id);

    void delete(UUID id);
}
