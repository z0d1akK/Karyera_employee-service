package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.request.create.EducationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EducationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EducationResponseDto;

import java.util.List;
import java.util.UUID;

public interface EducationService {

    EducationResponseDto add(UUID employeeId, EducationCreateRequestDto request);

    EducationResponseDto getById(UUID employeeId, UUID educationId);

    List<EducationResponseDto> listByEmployee(UUID employeeId);

    EducationResponseDto update(UUID employeeId, UUID educationId, EducationUpdateRequestDto request);

    void delete(UUID employeeId, UUID educationId);
}
