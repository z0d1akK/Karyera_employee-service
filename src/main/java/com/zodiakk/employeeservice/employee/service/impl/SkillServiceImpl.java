package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.SkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SkillFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SkillResponseDto;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.exception.skill.SkillAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillInUseException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.SkillMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeSkillRepository;
import com.zodiakk.employeeservice.employee.repository.SkillRepository;
import com.zodiakk.employeeservice.employee.repository.specification.skill.SkillPageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.skill.SkillSpecification;
import com.zodiakk.employeeservice.employee.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    private final EmployeeSkillRepository employeeSkillRepository;

    private final SkillMapper skillMapper;

    @Override
    @Transactional
    public SkillResponseDto create(SkillCreateRequestDto request) {
        validateNameUniqueness(request.getName());

        Skill skill = skillMapper.toEntity(request);
        skill.setActive(true);

        Skill savedSkill = skillRepository.save(skill);

        return skillMapper.toResponseDto(savedSkill);
    }

    @Override
    public SkillResponseDto getById(UUID id) {
        return skillMapper.toResponseDto(findById(id));
    }

    @Override
    public Page<SkillResponseDto> findAll(SkillFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = SkillPageableFactory.create(pageRequest);

        return skillRepository
                .findAll(SkillSpecification.filter(filter), pageable)
                .map(skillMapper::toResponseDto);
    }

    @Override
    @Transactional
    public SkillResponseDto update(UUID id, SkillUpdateRequestDto request) {
        Skill skill = findById(id);

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName().equalsIgnoreCase(skill.getName())) {

            validateNameUniqueness(request.getName());
        }

        skillMapper.updateEntity(request, skill);

        return skillMapper.toResponseDto(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Skill skill = findById(id);

        if (Boolean.TRUE.equals(skill.getActive())) {
            skill.setActive(false);
            skillRepository.save(skill);
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Skill skill = findById(id);

        if (Boolean.FALSE.equals(skill.getActive())) {
            skill.setActive(true);
            skillRepository.save(skill);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Skill skill = findById(id);

        if (employeeSkillRepository.existsBySkillId(id)) {
            throw new SkillInUseException(id);
        }

        skillRepository.delete(skill);
    }

    private Skill findById(UUID id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException(id));
    }

    private void validateNameUniqueness(String name) {
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new SkillAlreadyExistsException(name);
        }
    }
}
