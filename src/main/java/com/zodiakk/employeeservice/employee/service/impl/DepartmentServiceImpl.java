package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.DepartmentCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.DepartmentFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.DepartmentResponseDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentInUseException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.DepartmentMapper;
import com.zodiakk.employeeservice.employee.repository.DepartmentRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.service.DepartmentService;
import com.zodiakk.employeeservice.employee.repository.specification.department.DepartmentPageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.department.DepartmentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final EmployeeRepository employeeRepository;

    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponseDto create(DepartmentCreateRequestDto request) {
        validateNameUniqueness(request.getName());

        Department department = departmentMapper.toEntity(request);
        department.setActive(true);

        Department savedDepartment = departmentRepository.save(department);

        return departmentMapper.toResponseDto(savedDepartment);
    }

    @Override
    public DepartmentResponseDto getById(UUID id) {
        return departmentMapper.toResponseDto(findById(id));
    }

    @Override
    public Page<DepartmentResponseDto> findAll(DepartmentFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = DepartmentPageableFactory.create(pageRequest);

        return departmentRepository
                .findAll(DepartmentSpecification.filter(filter), pageable)
                .map(departmentMapper::toResponseDto);
    }

    @Override
    @Transactional
    public DepartmentResponseDto update(UUID id, DepartmentUpdateRequestDto request) {
        Department department = findById(id);

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName().equalsIgnoreCase(department.getName())) {

            validateNameUniqueness(request.getName());
        }

        departmentMapper.updateEntity(request, department);

        return departmentMapper.toResponseDto(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Department department = findById(id);

        if (Boolean.TRUE.equals(department.getActive())) {
            department.setActive(false);
            departmentRepository.save(department);
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Department department = findById(id);

        if (Boolean.FALSE.equals(department.getActive())) {
            department.setActive(true);
            departmentRepository.save(department);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Department department = findById(id);

        if (employeeRepository.countByDepartmentId(id) > 0) {
            throw new DepartmentInUseException(id);
        }

        departmentRepository.delete(department);
    }

    private Department findById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    private void validateNameUniqueness(String name) {
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DepartmentAlreadyExistsException(name);
        }
    }
}
