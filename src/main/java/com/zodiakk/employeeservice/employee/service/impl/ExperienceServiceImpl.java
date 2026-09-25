package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.ExperienceCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ExperienceUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ExperienceResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.Experience;
import com.zodiakk.employeeservice.employee.exception.common.InvalidDateRangeException;
import com.zodiakk.employeeservice.employee.exception.common.ResourceDoesNotBelongToEmployeeException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.experience.ExperienceNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.ExperienceMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.ExperienceRepository;
import com.zodiakk.employeeservice.employee.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;

    private final EmployeeRepository employeeRepository;

    private final ExperienceMapper experienceMapper;

    @Override
    @Transactional
    public ExperienceResponseDto add(UUID employeeId, ExperienceCreateRequestDto request) {
        Employee employee = findEmployee(employeeId);
        validateDateRange(request.getStartDate(), request.getEndDate());

        Experience experience = experienceMapper.toEntity(request);
        experience.setEmployee(employee);

        return experienceMapper.toResponseDto(experienceRepository.save(experience));
    }

    @Override
    public ExperienceResponseDto getById(UUID employeeId, UUID experienceId) {
        return experienceMapper.toResponseDto(findOwnedExperience(employeeId, experienceId));
    }

    @Override
    public List<ExperienceResponseDto> listByEmployee(UUID employeeId) {
        findEmployee(employeeId);
        return experienceRepository.findAllByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(experienceMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public ExperienceResponseDto update(UUID employeeId, UUID experienceId, ExperienceUpdateRequestDto request) {
        Experience experience = findOwnedExperience(employeeId, experienceId);

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : experience.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : experience.getEndDate();
        validateDateRange(startDate, endDate);

        experienceMapper.updateEntity(request, experience);

        return experienceMapper.toResponseDto(experienceRepository.save(experience));
    }

    @Override
    @Transactional
    public void delete(UUID employeeId, UUID experienceId) {
        Experience experience = findOwnedExperience(employeeId, experienceId);
        experienceRepository.delete(experience);
    }

    private Experience findOwnedExperience(UUID employeeId, UUID experienceId) {
        findEmployee(employeeId);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ExperienceNotFoundException(experienceId));

        if (!experience.getEmployee().getId().equals(employeeId)) {
            throw new ResourceDoesNotBelongToEmployeeException(employeeId);
        }

        return experience;
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
