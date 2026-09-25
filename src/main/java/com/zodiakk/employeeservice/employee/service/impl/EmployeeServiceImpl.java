package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.storage.ObjectStorageService;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.EmployeeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSummaryResponseDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeProfilePhoto;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import com.zodiakk.employeeservice.employee.entity.Grade;
import com.zodiakk.employeeservice.employee.entity.Position;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.catalog.CatalogInactiveException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeCannotBeCreatedTerminatedException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotTerminatedException;
import com.zodiakk.employeeservice.employee.exception.employee.InvalidEmployeeDatesException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.position.PositionNotFoundException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeMapper;
import com.zodiakk.employeeservice.employee.mapper.EmployeeResponsibilityMapper;
import com.zodiakk.employeeservice.employee.repository.DepartmentRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.GradeRepository;
import com.zodiakk.employeeservice.employee.repository.PositionRepository;
import com.zodiakk.employeeservice.employee.repository.SpecializationRepository;
import com.zodiakk.employeeservice.employee.repository.specification.employee.EmployeePageableFactory;
import com.zodiakk.employeeservice.employee.repository.specification.employee.EmployeeSpecification;
import com.zodiakk.employeeservice.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final PositionRepository positionRepository;

    private final GradeRepository gradeRepository;

    private final DepartmentRepository departmentRepository;

    private final SpecializationRepository specializationRepository;

    private final EmployeeResponsibilityRepository employeeResponsibilityRepository;

    private final ObjectStorageService objectStorageService;

    private final EmployeeMapper employeeMapper;

    private final EmployeeResponsibilityMapper employeeResponsibilityMapper;

    @Override
    @Transactional
    public EmployeeResponseDto create(EmployeeCreateRequestDto request) {
        if (employeeRepository.existsByUserId(request.getUserId())) {
            throw new EmployeeAlreadyExistsException(request.getUserId());
        }

        EmploymentStatus status = request.getEmploymentStatus() != null
                ? request.getEmploymentStatus()
                : EmploymentStatus.ACTIVE;

        if (status == EmploymentStatus.TERMINATED) {
            throw new EmployeeCannotBeCreatedTerminatedException();
        }

        validateDates(request.getBirthDate(), request.getHireDate());

        Employee employee = employeeMapper.toEntity(request);
        employee.setEmploymentStatus(status);
        employee.setPosition(resolveActivePosition(request.getPositionId()));
        employee.setGrade(resolveActiveGrade(request.getGradeId()));
        employee.setDepartment(resolveActiveDepartment(request.getDepartmentId()));
        employee.setSpecialization(resolveActiveSpecialization(request.getSpecializationId()));

        Employee saved = employeeRepository.save(employee);
        return toFullResponse(saved);
    }

    @Override
    public EmployeeResponseDto getById(UUID id) {
        return toFullResponse(findById(id));
    }

    @Override
    public EmployeeResponseDto getByUserId(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byUserId(userId));
        return toFullResponse(employee);
    }

    @Override
    public Page<EmployeeSummaryResponseDto> findAll(EmployeeFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = EmployeePageableFactory.create(pageRequest);

        return employeeRepository.findAll(EmployeeSpecification.filter(filter), pageable)
                .map(employeeMapper::toSummaryResponseDto);
    }

    @Override
    @Transactional
    public EmployeeResponseDto update(UUID id, EmployeeUpdateRequestDto request) {
        Employee employee = findById(id);

        LocalDate birthDate = request.getBirthDate() != null ? request.getBirthDate() : employee.getBirthDate();
        LocalDate hireDate = request.getHireDate() != null ? request.getHireDate() : employee.getHireDate();
        validateDates(birthDate, hireDate);

        EmploymentStatus previousStatus = employee.getEmploymentStatus();

        employeeMapper.updateEntity(request, employee);

        if (request.getPositionId() != null) {
            employee.setPosition(resolveActivePosition(request.getPositionId()));
        }
        if (request.getGradeId() != null) {
            employee.setGrade(resolveActiveGrade(request.getGradeId()));
        }
        if (request.getDepartmentId() != null) {
            employee.setDepartment(resolveActiveDepartment(request.getDepartmentId()));
        }
        if (request.getSpecializationId() != null) {
            employee.setSpecialization(resolveActiveSpecialization(request.getSpecializationId()));
        }

        if (request.getEmploymentStatus() == EmploymentStatus.TERMINATED
                && previousStatus != EmploymentStatus.TERMINATED) {
            closeActiveResponsibilities(employee.getId());
        }

        return toFullResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDto changeStatus(UUID id, EmploymentStatus status) {
        Employee employee = findById(id);
        EmploymentStatus previousStatus = employee.getEmploymentStatus();

        employee.setEmploymentStatus(status);

        if (status == EmploymentStatus.TERMINATED && previousStatus != EmploymentStatus.TERMINATED) {
            closeActiveResponsibilities(id);
        }

        return toFullResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Employee employee = findById(id);

        if (employee.getEmploymentStatus() != EmploymentStatus.TERMINATED) {
            throw new EmployeeNotTerminatedException(id);
        }

        EmployeeProfilePhoto photo = employee.getProfilePhoto();
        if (photo != null && photo.getObjectKey() != null) {
            objectStorageService.delete(photo.getObjectKey());
        }

        employeeResponsibilityRepository.deleteByEmployeeIdOrResponsibleEmployeeId(id, id);
        employeeRepository.delete(employee);
    }

    private EmployeeResponseDto toFullResponse(Employee employee) {
        EmployeeResponseDto response = employeeMapper.toResponseDto(employee);
        List<EmployeeResponsibility> responsibilities =
                employeeResponsibilityRepository.findAllByEmployeeId(employee.getId());
        response.setResponsibilities(
                responsibilities.stream()
                        .map(employeeResponsibilityMapper::toResponseDto)
                        .toList()
        );
        return response;
    }

    private void closeActiveResponsibilities(UUID employeeId) {
        LocalDate today = LocalDate.now();
        List<EmployeeResponsibility> active =
                employeeResponsibilityRepository.findActiveInvolvingEmployee(employeeId);

        for (EmployeeResponsibility responsibility : active) {
            if (responsibility.getEndDate() == null) {
                responsibility.setEndDate(today);
            }
        }

        employeeResponsibilityRepository.saveAll(active);
    }

    private void validateDates(LocalDate birthDate, LocalDate hireDate) {
        if (hireDate == null) {
            return;
        }

        if (hireDate.isAfter(LocalDate.now())) {
            throw new InvalidEmployeeDatesException();
        }

        if (birthDate != null && !birthDate.isBefore(hireDate)) {
            throw new InvalidEmployeeDatesException();
        }
    }

    private Position resolveActivePosition(UUID positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new PositionNotFoundException(positionId));
        requireActive("Position", positionId, position.getActive());
        return position;
    }

    private Grade resolveActiveGrade(UUID gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));
        requireActive("Grade", gradeId, grade.getActive());
        return grade;
    }

    private Department resolveActiveDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        requireActive("Department", departmentId, department.getActive());
        return department;
    }

    private Specialization resolveActiveSpecialization(UUID specializationId) {
        if (specializationId == null) {
            return null;
        }
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException(specializationId));
        requireActive("Specialization", specializationId, specialization.getActive());
        return specialization;
    }

    private void requireActive(String catalogName, UUID id, Boolean active) {
        if (!Boolean.TRUE.equals(active)) {
            throw new CatalogInactiveException(catalogName, id);
        }
    }

    private Employee findById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}
