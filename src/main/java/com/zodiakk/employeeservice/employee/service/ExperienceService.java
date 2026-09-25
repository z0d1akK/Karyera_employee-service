package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.request.create.ExperienceCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ExperienceUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ExperienceResponseDto;

import java.util.List;
import java.util.UUID;

public interface ExperienceService {

    ExperienceResponseDto add(UUID employeeId, ExperienceCreateRequestDto request);

    ExperienceResponseDto getById(UUID employeeId, UUID experienceId);

    List<ExperienceResponseDto> listByEmployee(UUID employeeId);

    ExperienceResponseDto update(UUID employeeId, UUID experienceId, ExperienceUpdateRequestDto request);

    void delete(UUID employeeId, UUID experienceId);
}
