package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.SpecializationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SpecializationFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SpecializationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SpecializationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationInUseException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.SpecializationMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.SpecializationRepository;
import com.zodiakk.employeeservice.employee.repository.specification.specialization.SpecializationPageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.specialization.SpecializationSpecification;
import com.zodiakk.employeeservice.employee.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecializationServiceImpl implements SpecializationService {

    private final SpecializationRepository specializationRepository;

    private final EmployeeRepository employeeRepository;

    private final SpecializationMapper specializationMapper;

    @Override
    @Transactional
    public SpecializationResponseDto create(SpecializationCreateRequestDto request) {
        validateNameUniqueness(request.getName());

        Specialization specialization = specializationMapper.toEntity(request);
        specialization.setActive(true);

        Specialization savedSpecialization = specializationRepository.save(specialization);

        return specializationMapper.toResponseDto(savedSpecialization);
    }

    @Override
    public SpecializationResponseDto getById(UUID id) {
        return specializationMapper.toResponseDto(findById(id));
    }

    @Override
    public Page<SpecializationResponseDto> findAll(SpecializationFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = SpecializationPageableFactory.create(pageRequest);

        return specializationRepository
                .findAll(SpecializationSpecification.filter(filter), pageable)
                .map(specializationMapper::toResponseDto);
    }

    @Override
    @Transactional
    public SpecializationResponseDto update(UUID id, SpecializationUpdateRequestDto request) {
        Specialization specialization = findById(id);

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName().equalsIgnoreCase(specialization.getName())) {

            validateNameUniqueness(request.getName());
        }

        specializationMapper.updateEntity(request, specialization);

        return specializationMapper.toResponseDto(specializationRepository.save(specialization));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Specialization specialization = findById(id);

        if (Boolean.TRUE.equals(specialization.getActive())) {
            specialization.setActive(false);
            specializationRepository.save(specialization);
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Specialization specialization = findById(id);

        if (Boolean.FALSE.equals(specialization.getActive())) {
            specialization.setActive(true);
            specializationRepository.save(specialization);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Specialization specialization = findById(id);

        if (employeeRepository.countBySpecializationId(id) > 0) {
            throw new SpecializationInUseException(id);
        }

        specializationRepository.delete(specialization);
    }

    private Specialization findById(UUID id) {
        return specializationRepository.findById(id)
                .orElseThrow(() -> new SpecializationNotFoundException(id));
    }

    private void validateNameUniqueness(String name) {
        if (specializationRepository.existsByNameIgnoreCase(name)) {
            throw new SpecializationAlreadyExistsException(name);
        }
    }
}
