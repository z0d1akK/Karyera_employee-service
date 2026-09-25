package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.GradeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.GradeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.GradeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.GradeResponseDto;
import com.zodiakk.employeeservice.employee.entity.Grade;
import com.zodiakk.employeeservice.employee.exception.grade.GradeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeInUseException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.GradeMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.GradeRepository;
import com.zodiakk.employeeservice.employee.repository.specification.grade.GradePageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.grade.GradeSpecification;
import com.zodiakk.employeeservice.employee.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

    private final EmployeeRepository employeeRepository;

    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public GradeResponseDto create(GradeCreateRequestDto request) {
        validateUniqueness(request.getName(), request.getLevel());

        Grade grade = gradeMapper.toEntity(request);
        grade.setActive(true);

        Grade savedGrade = gradeRepository.save(grade);

        return gradeMapper.toResponseDto(savedGrade);
    }

    @Override
    public GradeResponseDto getById(UUID id) {
        return gradeMapper.toResponseDto(findById(id));
    }

    @Override
    public Page<GradeResponseDto> findAll(GradeFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = GradePageableFactory.create(pageRequest);

        return gradeRepository
                .findAll(GradeSpecification.filter(filter), pageable)
                .map(gradeMapper::toResponseDto);
    }

    @Override
    @Transactional
    public GradeResponseDto update(UUID id, GradeUpdateRequestDto request) {
        Grade grade = findById(id);

        boolean nameChanged = request.getName() != null
                && !request.getName().isBlank()
                && !request.getName().equalsIgnoreCase(grade.getName());

        boolean levelChanged = request.getLevel() != null
                && !request.getLevel().equals(grade.getLevel());

        if (nameChanged || levelChanged) {
            String nameToCheck = nameChanged ? request.getName() : grade.getName();
            Integer levelToCheck = levelChanged ? request.getLevel() : grade.getLevel();
            validateUniqueness(nameToCheck, levelToCheck, nameChanged, levelChanged);
        }

        gradeMapper.updateEntity(request, grade);

        return gradeMapper.toResponseDto(gradeRepository.save(grade));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Grade grade = findById(id);

        if (Boolean.TRUE.equals(grade.getActive())) {
            grade.setActive(false);
            gradeRepository.save(grade);
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Grade grade = findById(id);

        if (Boolean.FALSE.equals(grade.getActive())) {
            grade.setActive(true);
            gradeRepository.save(grade);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Grade grade = findById(id);

        if (employeeRepository.countByGradeId(id) > 0) {
            throw new GradeInUseException(id);
        }

        gradeRepository.delete(grade);
    }

    private Grade findById(UUID id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new GradeNotFoundException(id));
    }

    private void validateUniqueness(String name, Integer level) {
        validateUniqueness(name, level, true, true);
    }

    private void validateUniqueness(String name, Integer level, boolean checkName, boolean checkLevel) {
        if (checkName && gradeRepository.existsByNameIgnoreCase(name)) {
            throw new GradeAlreadyExistsException();
        }

        if (checkLevel && gradeRepository.existsByLevel(level)) {
            throw new GradeAlreadyExistsException();
        }
    }
}
