package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.response.EmployeeAccessScopeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.ReferenceLookupResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.SubordinateIdsResponseDto;

import java.util.List;
import java.util.UUID;

public interface InternalEmployeeReferenceService {

    ReferenceLookupResponseDto lookupPositions(List<UUID> ids);

    ReferenceLookupResponseDto lookupGrades(List<UUID> ids);

    ReferenceLookupResponseDto lookupSkills(List<UUID> ids);

    EmployeeAccessScopeResponseDto getAccessScope(UUID employeeId, UUID actorEmployeeId);

    SubordinateIdsResponseDto listSubordinateIds(UUID employeeId);
}
