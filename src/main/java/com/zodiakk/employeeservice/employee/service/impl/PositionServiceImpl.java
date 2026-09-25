package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.PositionCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.PositionFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.PositionUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.PositionResponseDto;
import com.zodiakk.employeeservice.employee.entity.Position;
import com.zodiakk.employeeservice.employee.exception.position.PositionAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.position.PositionInUseException;
import com.zodiakk.employeeservice.employee.exception.position.PositionNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.PositionMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.PositionRepository;
import com.zodiakk.employeeservice.employee.repository.specification.position.PositionPageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.position.PositionSpecification;
import com.zodiakk.employeeservice.employee.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    private final EmployeeRepository employeeRepository;

    private final PositionMapper positionMapper;

    @Override
    @Transactional
    public PositionResponseDto create(PositionCreateRequestDto request) {
        validateNameUniqueness(request.getName());

        Position position = positionMapper.toEntity(request);
        position.setActive(true);

        Position savedPosition = positionRepository.save(position);

        return positionMapper.toResponseDto(savedPosition);
    }

    @Override
    public PositionResponseDto getById(UUID id) {
        return positionMapper.toResponseDto(findById(id));
    }

    @Override
    public Page<PositionResponseDto> findAll(PositionFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = PositionPageableFactory.create(pageRequest);

        return positionRepository
                .findAll(PositionSpecification.filter(filter), pageable)
                .map(positionMapper::toResponseDto);
    }

    @Override
    @Transactional
    public PositionResponseDto update(UUID id, PositionUpdateRequestDto request) {
        Position position = findById(id);

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName().equalsIgnoreCase(position.getName())) {

            validateNameUniqueness(request.getName());
        }

        positionMapper.updateEntity(request, position);

        return positionMapper.toResponseDto(positionRepository.save(position));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Position position = findById(id);

        if (Boolean.TRUE.equals(position.getActive())) {
            position.setActive(false);
            positionRepository.save(position);
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Position position = findById(id);

        if (Boolean.FALSE.equals(position.getActive())) {
            position.setActive(true);
            positionRepository.save(position);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Position position = findById(id);

        if (employeeRepository.countByPositionId(id) > 0) {
            throw new PositionInUseException(id);
        }

        positionRepository.delete(position);
    }

    private Position findById(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
    }

    private void validateNameUniqueness(String name) {
        if (positionRepository.existsByNameIgnoreCase(name)) {
            throw new PositionAlreadyExistsException(name);
        }
    }
}
