package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.response.EmployeeAccessScopeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.ReferenceLookupItemDto;
import com.zodiakk.employeeservice.employee.dto.response.ReferenceLookupResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.SubordinateIdsResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.GradeRepository;
import com.zodiakk.employeeservice.employee.repository.PositionRepository;
import com.zodiakk.employeeservice.employee.repository.SkillRepository;
import com.zodiakk.employeeservice.employee.service.InternalEmployeeReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalEmployeeReferenceServiceImpl implements InternalEmployeeReferenceService {

    private final PositionRepository positionRepository;

    private final GradeRepository gradeRepository;

    private final SkillRepository skillRepository;

    private final EmployeeRepository employeeRepository;

    private final EmployeeResponsibilityRepository employeeResponsibilityRepository;

    @Override
    public ReferenceLookupResponseDto lookupPositions(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ReferenceLookupResponseDto(List.of());
        }

        List<ReferenceLookupItemDto> items = positionRepository.findAllById(new LinkedHashSet<>(ids))
                .stream()
                .map(position -> new ReferenceLookupItemDto(
                        position.getId(),
                        position.getName(),
                        position.getActive()
                ))
                .toList();

        return new ReferenceLookupResponseDto(items);
    }

    @Override
    public ReferenceLookupResponseDto lookupGrades(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ReferenceLookupResponseDto(List.of());
        }

        List<ReferenceLookupItemDto> items = gradeRepository.findAllById(new LinkedHashSet<>(ids))
                .stream()
                .map(grade -> new ReferenceLookupItemDto(
                        grade.getId(),
                        grade.getName(),
                        grade.getActive()
                ))
                .toList();

        return new ReferenceLookupResponseDto(items);
    }

    @Override
    public ReferenceLookupResponseDto lookupSkills(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ReferenceLookupResponseDto(List.of());
        }

        List<ReferenceLookupItemDto> items = skillRepository.findAllById(new LinkedHashSet<>(ids))
                .stream()
                .map(skill -> new ReferenceLookupItemDto(
                        skill.getId(),
                        skill.getName(),
                        skill.getActive()
                ))
                .toList();

        return new ReferenceLookupResponseDto(items);
    }

    @Override
    public EmployeeAccessScopeResponseDto getAccessScope(UUID employeeId, UUID actorEmployeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        boolean self = actorEmployeeId != null && actorEmployeeId.equals(employeeId);

        boolean managerOfEmployee = actorEmployeeId != null
                && !self
                && employeeResponsibilityRepository
                .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(employeeId, actorEmployeeId);

        return new EmployeeAccessScopeResponseDto(
                employeeId,
                actorEmployeeId,
                self,
                managerOfEmployee,
                employee.getEmploymentStatus() == EmploymentStatus.ACTIVE
        );
    }

    @Override
    public SubordinateIdsResponseDto listSubordinateIds(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }

        return new SubordinateIdsResponseDto(
                employeeId,
                employeeResponsibilityRepository.findActiveSubordinateIds(employeeId)
        );
    }
}
