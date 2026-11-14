package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.response.ActiveEmployeeIdsResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.CareerProfileLookupResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.CareerProfileResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeSkill;
import com.zodiakk.employeeservice.employee.entity.Experience;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeSkillRepository;
import com.zodiakk.employeeservice.employee.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerProfileService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeSkillRepository employeeSkillRepository;

    private final ExperienceRepository experienceRepository;

    public CareerProfileResponseDto get(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return toProfile(employee);
    }

    public CareerProfileLookupResponseDto lookup(List<UUID> employeeIds) {
        List<CareerProfileResponseDto> profiles = new ArrayList<>();

        if (employeeIds != null) {
            for (UUID employeeId : employeeIds) {
                employeeRepository.findById(employeeId).ifPresent(employee -> profiles.add(toProfile(employee)));
            }
        }

        return new CareerProfileLookupResponseDto(profiles);
    }

    public ActiveEmployeeIdsResponseDto activeIds() {
        return new ActiveEmployeeIdsResponseDto(
                employeeRepository.findIdsByEmploymentStatus(EmploymentStatus.ACTIVE)
        );
    }

    public ActiveEmployeeIdsResponseDto filterIds(
            boolean activeOnly,
            UUID positionId,
            UUID gradeId,
            UUID departmentId
    ) {
        return new ActiveEmployeeIdsResponseDto(
                employeeRepository.filterIds(activeOnly, positionId, gradeId, departmentId)
        );
    }

    private CareerProfileResponseDto toProfile(Employee employee) {
        List<EmployeeSkill> skills = employeeSkillRepository.findAllByEmployeeId(employee.getId());
        List<Experience> experiences = experienceRepository.findAllByEmployeeIdOrderByStartDateDesc(employee.getId());

        return new CareerProfileResponseDto(
                employee.getId(),
                employee.getPosition().getId(),
                employee.getPosition().getName(),
                employee.getGrade().getId(),
                employee.getGrade().getName(),
                employee.getEmploymentStatus() == EmploymentStatus.ACTIVE,
                employee.getVersion() == null ? 0L : employee.getVersion(),
                skills.stream()
                        .map(skill -> new CareerProfileResponseDto.SkillLevel(
                                skill.getSkill().getId(),
                                skill.getSkill().getName(),
                                skill.getLevel()
                        ))
                        .toList(),
                experiences.stream()
                        .map(experience -> new CareerProfileResponseDto.ExperiencePeriod(
                                experience.getStartDate(),
                                experience.getEndDate()
                        ))
                        .toList(),
                fullName(employee)
        );
    }

    private String fullName(Employee employee) {
        StringBuilder builder = new StringBuilder();
        if (employee.getLastName() != null) {
            builder.append(employee.getLastName());
        }
        if (employee.getFirstName() != null && !employee.getFirstName().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(employee.getFirstName());
        }
        if (employee.getMiddleName() != null && !employee.getMiddleName().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(employee.getMiddleName());
        }
        return builder.toString();
    }
}
