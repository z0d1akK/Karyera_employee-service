package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.EducationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EducationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EducationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Education;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.exception.common.InvalidDateRangeException;
import com.zodiakk.employeeservice.employee.exception.common.ResourceDoesNotBelongToEmployeeException;
import com.zodiakk.employeeservice.employee.exception.education.EducationNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EducationMapper;
import com.zodiakk.employeeservice.employee.repository.EducationRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;

    private final EmployeeRepository employeeRepository;

    private final EducationMapper educationMapper;

    @Override
    @Transactional
    public EducationResponseDto add(UUID employeeId, EducationCreateRequestDto request) {
        Employee employee = findEmployee(employeeId);
        validateDateRange(request.getStartDate(), request.getEndDate());

        Education education = educationMapper.toEntity(request);
        education.setEmployee(employee);

        return educationMapper.toResponseDto(educationRepository.save(education));
    }

    @Override
    public EducationResponseDto getById(UUID employeeId, UUID educationId) {
        return educationMapper.toResponseDto(findOwnedEducation(employeeId, educationId));
    }

    @Override
    public List<EducationResponseDto> listByEmployee(UUID employeeId) {
        findEmployee(employeeId);
        return educationRepository.findAllByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(educationMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EducationResponseDto update(UUID employeeId, UUID educationId, EducationUpdateRequestDto request) {
        Education education = findOwnedEducation(employeeId, educationId);

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : education.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : education.getEndDate();
        validateDateRange(startDate, endDate);

        educationMapper.updateEntity(request, education);

        return educationMapper.toResponseDto(educationRepository.save(education));
    }

    @Override
    @Transactional
    public void delete(UUID employeeId, UUID educationId) {
        Education education = findOwnedEducation(employeeId, educationId);
        educationRepository.delete(education);
    }

    private Education findOwnedEducation(UUID employeeId, UUID educationId) {
        findEmployee(employeeId);
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new EducationNotFoundException(educationId));

        if (!education.getEmployee().getId().equals(employeeId)) {
            throw new ResourceDoesNotBelongToEmployeeException(employeeId);
        }

        return education;
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException();
        }
    }
}
