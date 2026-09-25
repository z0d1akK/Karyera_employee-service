package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.request.create.MilitaryObligationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.MilitaryObligationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.MilitaryObligationResponseDto;

import java.util.UUID;

public interface MilitaryObligationService {

    MilitaryObligationResponseDto create(UUID employeeId, MilitaryObligationCreateRequestDto request);

    MilitaryObligationResponseDto getByEmployeeId(UUID employeeId);

    MilitaryObligationResponseDto update(UUID employeeId, MilitaryObligationUpdateRequestDto request);

    MilitaryObligationResponseDto upsert(UUID employeeId, MilitaryObligationCreateRequestDto request);

    void delete(UUID employeeId);
}
