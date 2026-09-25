package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.ResponsibilityTypeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.ResponsibilityTypeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ResponsibilityTypeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ResponsibilityTypeResponseDto;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeInUseException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.ResponsibilityTypeMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.ResponsibilityTypeRepository;
import com.zodiakk.employeeservice.employee.repository.specification.responsibilitytype.ResponsibilityTypePageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.responsibilitytype.ResponsibilityTypeSpecification;
import com.zodiakk.employeeservice.employee.service.ResponsibilityTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResponsibilityTypeServiceImpl implements ResponsibilityTypeService {

    private final ResponsibilityTypeRepository responsibilityTypeRepository;

    private final EmployeeResponsibilityRepository employeeResponsibilityRepository;

    private final ResponsibilityTypeMapper responsibilityTypeMapper;

    @Override
    @Transactional
    public ResponsibilityTypeResponseDto create(ResponsibilityTypeCreateRequestDto request) {
        validateCodeUniqueness(request.getCode());

        ResponsibilityType responsibilityType = responsibilityTypeMapper.toEntity(request);
        responsibilityType.setActive(true);

        ResponsibilityType saved = responsibilityTypeRepository.save(responsibilityType);

        return responsibilityTypeMapper.toResponseDto(saved);
    }

    @Override
    public ResponsibilityTypeResponseDto getById(UUID id) {
        return responsibilityTypeMapper.toResponseDto(findById(id));
    }

    @Override
    public Page<ResponsibilityTypeResponseDto> findAll(
            ResponsibilityTypeFilterDto filter,
            PageRequestDto pageRequest
    ) {
        Pageable pageable = ResponsibilityTypePageableFactory.create(pageRequest);

        return responsibilityTypeRepository
                .findAll(ResponsibilityTypeSpecification.filter(filter), pageable)
                .map(responsibilityTypeMapper::toResponseDto);
    }

    @Override
    @Transactional
    public ResponsibilityTypeResponseDto update(UUID id, ResponsibilityTypeUpdateRequestDto request) {
        ResponsibilityType responsibilityType = findById(id);

        if (request.getCode() != null
                && !request.getCode().isBlank()
                && !request.getCode().equalsIgnoreCase(responsibilityType.getCode())) {

            validateCodeUniqueness(request.getCode());
        }

        responsibilityTypeMapper.updateEntity(request, responsibilityType);

        return responsibilityTypeMapper.toResponseDto(responsibilityTypeRepository.save(responsibilityType));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        ResponsibilityType responsibilityType = findById(id);

        if (Boolean.TRUE.equals(responsibilityType.getActive())) {
            responsibilityType.setActive(false);
            responsibilityTypeRepository.save(responsibilityType);
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        ResponsibilityType responsibilityType = findById(id);

        if (Boolean.FALSE.equals(responsibilityType.getActive())) {
            responsibilityType.setActive(true);
            responsibilityTypeRepository.save(responsibilityType);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ResponsibilityType responsibilityType = findById(id);

        if (employeeResponsibilityRepository.existsByResponsibilityTypeId(id)) {
            throw new ResponsibilityTypeInUseException(id);
        }

        responsibilityTypeRepository.delete(responsibilityType);
    }

    private ResponsibilityType findById(UUID id) {
        return responsibilityTypeRepository.findById(id)
                .orElseThrow(() -> new ResponsibilityTypeNotFoundException(id));
    }

    private void validateCodeUniqueness(String code) {
        if (responsibilityTypeRepository.existsByCode(code)) {
            throw new ResponsibilityTypeAlreadyExistsException(code);
        }
    }
}
