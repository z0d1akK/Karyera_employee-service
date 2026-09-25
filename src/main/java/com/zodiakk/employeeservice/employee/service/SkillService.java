package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.SkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SkillFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SkillResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SkillService {

    SkillResponseDto create(SkillCreateRequestDto request);

    SkillResponseDto getById(UUID id);

    Page<SkillResponseDto> findAll(SkillFilterDto filter, PageRequestDto pageRequest);

    SkillResponseDto update(UUID id, SkillUpdateRequestDto request);

    void deactivate(UUID id);

    void activate(UUID id);

    void delete(UUID id);
}
