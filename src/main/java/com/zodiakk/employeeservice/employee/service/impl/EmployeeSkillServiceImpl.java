package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.constants.CatalogNames;
import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeSkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeSkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSkillResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeSkill;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.exception.catalog.CatalogInactiveException;
import com.zodiakk.employeeservice.employee.exception.common.ResourceDoesNotBelongToEmployeeException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employeeskill.EmployeeSkillAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.employeeskill.EmployeeSkillNotFoundException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeSkillMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeSkillRepository;
import com.zodiakk.employeeservice.employee.repository.SkillRepository;
import com.zodiakk.employeeservice.employee.service.EmployeeSkillService;
import com.zodiakk.employeeservice.integration.employee.service.EmployeeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.zodiakk.employeeservice.common.cache.CacheNames.EMPLOYEES;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeSkillRepository employeeSkillRepository;

    private final EmployeeRepository employeeRepository;

    private final SkillRepository skillRepository;

    private final EmployeeSkillMapper employeeSkillMapper;

    private final EmployeeEventService employeeEventService;

    @Override
    @PreAuthorize("@employeeAccess.canEditEmployee(#employeeId)")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
    public EmployeeSkillResponseDto add(UUID employeeId, EmployeeSkillCreateRequestDto request) {
        Employee employee = findEmployee(employeeId);
        Skill skill = resolveActiveSkill(request.getSkillId());

        if (employeeSkillRepository.existsByEmployeeIdAndSkillId(
                employeeId,
                skill.getId()
        )) {
            throw new EmployeeSkillAlreadyExistsException(skill.getId());
        }

        EmployeeSkill employeeSkill = employeeSkillMapper.toEntity(request);

        employeeSkill.setEmployee(employee);
        employeeSkill.setSkill(skill);
        employeeSkill.setLastAssessedAt(LocalDate.now());

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);

        employeeEventService.recordSkillUpdated(
                employeeId,
                skill.getId(),
                saved.getLevel(),
                saved.getYearsOfExperience(),
                saved.getLastAssessedAt(),
                KafkaEventConstants.SKILL_CHANGE_ADDED
        );

        return employeeSkillMapper.toResponseDto(saved);
    }

    @Override
    @PreAuthorize("@employeeAccess.canViewEmployee(#employeeId)")
    public List<EmployeeSkillResponseDto> listByEmployee(UUID employeeId) {
        findEmployee(employeeId);

        return employeeSkillRepository
                .findAllByEmployeeId(employeeId)
                .stream()
                .map(employeeSkillMapper::toResponseDto)
                .toList();
    }

    @Override
    @PreAuthorize("@employeeAccess.canEditEmployee(#employeeId)")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
    public EmployeeSkillResponseDto update(UUID employeeId, UUID employeeSkillId, EmployeeSkillUpdateRequestDto request) {
        EmployeeSkill employeeSkill = findOwnedEmployeeSkill(employeeId, employeeSkillId);

        boolean levelChanged =
                request.getLevel() != null
                        && !request.getLevel()
                        .equals(employeeSkill.getLevel());

        employeeSkillMapper.updateEntity(request, employeeSkill);

        if (levelChanged) {
            employeeSkill.setLastAssessedAt(LocalDate.now());
        }

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);

        employeeEventService.recordSkillUpdated(
                employeeId,
                saved.getSkill().getId(),
                saved.getLevel(),
                saved.getYearsOfExperience(),
                saved.getLastAssessedAt(),
                KafkaEventConstants.SKILL_CHANGE_UPDATED
        );

        return employeeSkillMapper.toResponseDto(saved);
    }

    @Override
    @PreAuthorize("@employeeAccess.canEditEmployee(#employeeId)")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
    public void remove(UUID employeeId, UUID employeeSkillId) {
        EmployeeSkill employeeSkill = findOwnedEmployeeSkill(employeeId, employeeSkillId);

        UUID skillId = employeeSkill.getSkill().getId();
        Integer level = employeeSkill.getLevel();
        BigDecimal yearsOfExperience = employeeSkill.getYearsOfExperience();
        LocalDate lastAssessedAt = employeeSkill.getLastAssessedAt();

        employeeSkillRepository.delete(employeeSkill);

        employeeEventService.recordSkillUpdated(
                employeeId,
                skillId,
                level,
                yearsOfExperience,
                lastAssessedAt,
                KafkaEventConstants.SKILL_CHANGE_REMOVED
        );
    }

    private EmployeeSkill findOwnedEmployeeSkill(UUID employeeId, UUID employeeSkillId) {
        findEmployee(employeeId);

        EmployeeSkill employeeSkill =
                employeeSkillRepository.findById(employeeSkillId)
                        .orElseThrow(() -> new EmployeeSkillNotFoundException(employeeSkillId));

        if (!employeeSkill.getEmployee().getId().equals(employeeId)) {
            throw new ResourceDoesNotBelongToEmployeeException(employeeId);
        }

        return employeeSkill;
    }

    private Skill resolveActiveSkill(UUID skillId) {
        Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new SkillNotFoundException(skillId));

        if (!Boolean.TRUE.equals(skill.getActive())) {
            throw new CatalogInactiveException(
                    CatalogNames.SKILL,
                    skillId
            );
        }

        return skill;
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}
