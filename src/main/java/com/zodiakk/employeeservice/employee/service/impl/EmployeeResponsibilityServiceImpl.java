package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeResponsibilityCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeResponsibilityUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponsibilityResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.catalog.CatalogInactiveException;
import com.zodiakk.employeeservice.employee.exception.common.InvalidDateRangeException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeTerminatedException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.EmployeeResponsibilityNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.MaxSubordinatesExceededException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.MultipleNotAllowedException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.ResponsibilityAlreadyActiveException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.SelfResponsibilityException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeResponsibilityMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.ResponsibilityTypeRepository;
import com.zodiakk.employeeservice.employee.service.EmployeeResponsibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeResponsibilityServiceImpl implements EmployeeResponsibilityService {

    private final EmployeeResponsibilityRepository employeeResponsibilityRepository;

    private final EmployeeRepository employeeRepository;

    private final ResponsibilityTypeRepository responsibilityTypeRepository;

    private final EmployeeResponsibilityMapper employeeResponsibilityMapper;

    @Override
    @Transactional
    public EmployeeResponsibilityResponseDto assign(
            UUID employeeId,
            EmployeeResponsibilityCreateRequestDto request) {
        Employee employee = findActiveEmployee(employeeId);

        if (employeeId.equals(request.getResponsibleEmployeeId())) {
            throw new SelfResponsibilityException(employeeId);
        }

        Employee responsibleEmployee = findActiveEmployee(request.getResponsibleEmployeeId());

        ResponsibilityType responsibilityType = resolveActiveType(request.getResponsibilityTypeId());

        validateDateRange(request.getStartDate(), request.getEndDate());

        validateAssignmentRules(
                employeeId,
                responsibleEmployee,
                responsibilityType,
                null
        );

        EmployeeResponsibility responsibility = employeeResponsibilityMapper.toEntity(request);

        responsibility.setEmployee(employee);
        responsibility.setResponsibleEmployee(responsibleEmployee);
        responsibility.setResponsibilityType(responsibilityType);

        return employeeResponsibilityMapper.toResponseDto(
                employeeResponsibilityRepository.save(responsibility));
    }

    @Override
    public List<EmployeeResponsibilityResponseDto> listByEmployee(UUID employeeId) {
        findEmployee(employeeId);

        return employeeResponsibilityRepository
                .findAllByEmployeeId(employeeId)
                .stream()
                .map(employeeResponsibilityMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<EmployeeResponsibilityResponseDto> listByResponsible(UUID responsibleEmployeeId) {
        findEmployee(responsibleEmployeeId);

        return employeeResponsibilityRepository
                .findAllByResponsibleEmployeeId(responsibleEmployeeId)
                .stream()
                .map(employeeResponsibilityMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponsibilityResponseDto close(UUID responsibilityId, LocalDate endDate) {
        EmployeeResponsibility responsibility =
                findResponsibility(responsibilityId);

        LocalDate effectiveEndDate =
                endDate != null ? endDate : LocalDate.now();

        validateDateRange(responsibility.getStartDate(), effectiveEndDate);

        responsibility.setEndDate(effectiveEndDate);

        return employeeResponsibilityMapper.toResponseDto(
                employeeResponsibilityRepository.save(responsibility)
        );
    }

    @Override
    @Transactional
    public EmployeeResponsibilityResponseDto update(UUID responsibilityId,
            EmployeeResponsibilityUpdateRequestDto request
    ) {
        EmployeeResponsibility responsibility = findResponsibility(responsibilityId);

        UUID employeeId = responsibility.getEmployee().getId();

        Employee responsibleEmployee =
                request.getResponsibleEmployeeId() != null
                        ? findActiveEmployee(request.getResponsibleEmployeeId())
                        : responsibility.getResponsibleEmployee();

        if (employeeId.equals(responsibleEmployee.getId())) {
            throw new SelfResponsibilityException(employeeId);
        }

        ResponsibilityType responsibilityType =
                request.getResponsibilityTypeId() != null
                        ? resolveActiveType(request.getResponsibilityTypeId())
                        : responsibility.getResponsibilityType();

        LocalDate startDate =
                request.getStartDate() != null
                        ? request.getStartDate()
                        : responsibility.getStartDate();

        LocalDate endDate =
                request.getEndDate() != null
                        ? request.getEndDate()
                        : responsibility.getEndDate();

        validateDateRange(startDate, endDate);

        boolean becomesActive =
                endDate == null && responsibility.getEndDate() != null;

        boolean assignmentChanged =
                !responsibleEmployee.getId()
                        .equals(responsibility.getResponsibleEmployee().getId())
                        || !responsibilityType.getId()
                        .equals(responsibility.getResponsibilityType().getId());

        if (endDate == null && (assignmentChanged || becomesActive)) {
            validateAssignmentRules(
                    employeeId,
                    responsibleEmployee,
                    responsibilityType,
                    responsibilityId
            );
        }

        employeeResponsibilityMapper.updateEntity(request, responsibility);

        responsibility.setResponsibleEmployee(responsibleEmployee);
        responsibility.setResponsibilityType(responsibilityType);

        return employeeResponsibilityMapper.toResponseDto(
                employeeResponsibilityRepository.save(responsibility)
        );
    }

    @Override
    @Transactional
    public void delete(UUID responsibilityId) {
        EmployeeResponsibility responsibility = findResponsibility(responsibilityId);

        employeeResponsibilityRepository.delete(responsibility);
    }

    private void validateAssignmentRules(
            UUID employeeId,
            Employee responsibleEmployee,
            ResponsibilityType responsibilityType,
            UUID excludeResponsibilityId
    ) {
        validateDuplicateAssignment(
                employeeId,
                responsibleEmployee,
                responsibilityType,
                excludeResponsibilityId
        );

        validateMultipleAllowed(
                responsibleEmployee,
                responsibilityType,
                excludeResponsibilityId
        );

        validateMaxSubordinates(
                responsibleEmployee,
                excludeResponsibilityId
        );
    }

    private void validateDuplicateAssignment(
            UUID employeeId,
            Employee responsibleEmployee,
            ResponsibilityType responsibilityType,
            UUID excludeResponsibilityId
    ) {
        var existing = employeeResponsibilityRepository
                .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                        employeeId,
                        responsibleEmployee.getId(),
                        responsibilityType.getId()
                );

        if (existing.isPresent()
                && !existing.get().getId().equals(excludeResponsibilityId)) {
            throw new ResponsibilityAlreadyActiveException();
        }
    }

    private void validateMultipleAllowed(
            Employee responsibleEmployee,
            ResponsibilityType responsibilityType,
            UUID excludeResponsibilityId
    ) {
        if (Boolean.TRUE.equals(responsibilityType.getMultipleAllowed())) {
            return;
        }

        long activeCount =
                employeeResponsibilityRepository
                        .countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull(
                                responsibleEmployee.getId(),
                                responsibilityType.getCode()
                        );

        if (excludeResponsibilityId != null) {
            var current =
                    employeeResponsibilityRepository.findById(
                            excludeResponsibilityId
                    );

            if (current.isPresent()
                    && current.get().getEndDate() == null
                    && current.get().getResponsibleEmployee()
                    .getId()
                    .equals(responsibleEmployee.getId())
                    && current.get().getResponsibilityType()
                    .getCode()
                    .equals(responsibilityType.getCode())) {
                activeCount--;
            }
        }

        if (activeCount > 0) {
            throw new MultipleNotAllowedException(
                    responsibilityType.getCode(),
                    responsibleEmployee.getId()
            );
        }
    }

    private void validateMaxSubordinates(Employee responsibleEmployee, UUID excludeResponsibilityId) {
        Integer maxSubordinates = responsibleEmployee.getMaxSubordinates();

        if (maxSubordinates == null) {
            return;
        }

        long activeSubordinates =
                employeeResponsibilityRepository
                        .countByResponsibleEmployeeIdAndEndDateIsNull(
                                responsibleEmployee.getId()
                        );

        if (excludeResponsibilityId != null) {
            var current =
                    employeeResponsibilityRepository.findById(
                            excludeResponsibilityId
                    );

            if (current.isPresent()
                    && current.get().getEndDate() == null
                    && current.get().getResponsibleEmployee()
                    .getId()
                    .equals(responsibleEmployee.getId())) {
                activeSubordinates--;
            }
        }

        if (activeSubordinates >= maxSubordinates) {
            throw new MaxSubordinatesExceededException(
                    responsibleEmployee.getId()
            );
        }
    }

    private ResponsibilityType resolveActiveType(UUID typeId) {
        ResponsibilityType type =
                responsibilityTypeRepository.findById(typeId)
                        .orElseThrow(
                                () -> new ResponsibilityTypeNotFoundException(typeId)
                        );

        if (!Boolean.TRUE.equals(type.getActive())) {
            throw new CatalogInactiveException(
                    "ResponsibilityType",
                    typeId
            );
        }

        return type;
    }

    private EmployeeResponsibility findResponsibility(UUID responsibilityId) {
        return employeeResponsibilityRepository.findById(responsibilityId)
                .orElseThrow(
                        () -> new EmployeeResponsibilityNotFoundException(
                                responsibilityId
                        )
                );
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(
                        () -> new EmployeeNotFoundException(employeeId)
                );
    }

    private Employee findActiveEmployee(UUID employeeId) {
        Employee employee = findEmployee(employeeId);

        if (employee.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
            throw new EmployeeTerminatedException(employeeId);
        }

        return employee;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException();
        }
    }
}