package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.constants.CatalogNames;
import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.storage.ObjectStorageService;
import com.zodiakk.employeeservice.common.util.EmailUtils;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.EmployeeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeLookupItemDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeLookupResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeRegistrationInfoDto;
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
import com.zodiakk.employeeservice.integration.employee.service.EmployeeEventService;
import com.zodiakk.employeeservice.security.access.EmployeeAccessService;
import com.zodiakk.employeeservice.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static com.zodiakk.employeeservice.common.cache.CacheNames.EMPLOYEES;

@Service
@RequiredArgsConstructor
@Slf4j
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

    private final EmployeeAccessService employeeAccessService;

    private final EmployeeEventService employeeEventService;

    @Override
    @PreAuthorize("@employeeAccess.canManageEmployeeProfiles()")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
    public EmployeeResponseDto create(EmployeeCreateRequestDto request) {
        if (request.getUserId() != null && employeeRepository.existsByUserId(request.getUserId())) {
            throw new EmployeeAlreadyExistsException(request.getUserId());
        }
        if (employeeRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmployeeAlreadyExistsException(request.getEmail());
        }

        EmploymentStatus status = request.getEmploymentStatus() != null
                ? request.getEmploymentStatus()
                : EmploymentStatus.ACTIVE;

        if (status == EmploymentStatus.TERMINATED) {
            throw new EmployeeCannotBeCreatedTerminatedException();
        }

        validateDates(request.getBirthDate(), request.getHireDate());

        Employee employee = employeeMapper.toEntity(request);

        String normalizedEmail = EmailUtils.normalize(request.getEmail());
        employee.setEmail(normalizedEmail);

        employee.setEmploymentStatus(status);
        employee.setPosition(resolveActivePosition(request.getPositionId()));
        employee.setGrade(resolveActiveGrade(request.getGradeId()));
        employee.setDepartment(resolveActiveDepartment(request.getDepartmentId()));
        employee.setSpecialization(resolveActiveSpecialization(request.getSpecializationId()));

        Employee saved = employeeRepository.save(employee);
        return toFullResponse(saved);
    }

    @Override
    @PreAuthorize("@employeeAccess.canViewEmployee(#id)")
    @Cacheable(cacheNames = EMPLOYEES, key = "'id:' + #id")
    public EmployeeResponseDto getById(UUID id) {
        return toFullResponse(findById(id));
    }

    @Override
    @PreAuthorize("@employeeAccess.canViewUser(#userId)")
    @Cacheable(cacheNames = EMPLOYEES, key = "'user:' + #userId")
    public EmployeeResponseDto getByUserId(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byUserId(userId));
        return toFullResponse(employee);
    }

    @Override
    @PreAuthorize("hasAnyRole('INTERNAL_SERVICE', 'HR', 'ADMIN')")
    public EmployeeRegistrationInfoDto getRegistrationInfo(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return new EmployeeRegistrationInfoDto(
                employee.getId(),
                employee.getEmail(),
                employee.getEmploymentStatus(),
                employee.getUserId()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('INTERNAL_SERVICE', 'HR', 'ADMIN')")
    public EmployeeLookupResponseDto lookup(List<UUID> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return new EmployeeLookupResponseDto(List.of());
        }

        List<Employee> employees = employeeRepository.findAllById(new LinkedHashSet<>(employeeIds));

        return new EmployeeLookupResponseDto(
                employees.stream()
                        .map(this::toLookupItem)
                        .toList()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('INTERNAL_SERVICE', 'HR', 'ADMIN')")
    public EmployeeLookupItemDto getLookupByUserId(UUID userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> EmployeeNotFoundException.byUserId(userId));

        return toLookupItem(employee);
    }

    @Override
    @PreAuthorize("@employeeAccess.canListEmployees()")
    public Page<EmployeeSummaryResponseDto> findAll(EmployeeFilterDto filter, PageRequestDto pageRequest) {
        Pageable pageable = EmployeePageableFactory.create(pageRequest);

        Specification<Employee> specification = EmployeeSpecification.filter(filter);

        if (employeeAccessService.isManagerOnly()) {
            specification = specification.and(
                    EmployeeSpecification.subordinatesOf(SecurityUtils.getCurrentUserId())
            );
        }

        return employeeRepository.findAll(specification, pageable)
                .map(employeeMapper::toSummaryResponseDto);
    }

    @Override
    @PreAuthorize("@employeeAccess.canEditEmployee(#id)")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
    public EmployeeResponseDto update(UUID id, EmployeeUpdateRequestDto request) {
        Employee employee = findById(id);

        validateSelfUpdateFields(request);

        LocalDate birthDate = request.getBirthDate() != null ? request.getBirthDate() : employee.getBirthDate();
        LocalDate hireDate = request.getHireDate() != null ? request.getHireDate() : employee.getHireDate();
        validateDates(birthDate, hireDate);

        EmploymentStatus previousStatus = employee.getEmploymentStatus();

        UUID previousPositionId = employee.getPosition() != null ? employee.getPosition().getId() : null;
        UUID previousGradeId = employee.getGrade() != null ? employee.getGrade().getId() : null;

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

        if (request.getEmail() != null) {
            if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
                throw new EmployeeAlreadyExistsException(request.getEmail());
            }
            String normalizedEmail = EmailUtils.normalize(request.getEmail());
            employee.setEmail(normalizedEmail);
        }

        Employee saved = employeeRepository.save(employee);

        UUID positionId = saved.getPosition() != null ? saved.getPosition().getId() : null;
        UUID gradeId = saved.getGrade() != null ? saved.getGrade().getId() : null;

        if (!Objects.equals(previousPositionId, positionId)
                || !Objects.equals(previousGradeId, gradeId)) {
            employeeEventService.recordPositionChanged(
                    saved.getId(),
                    previousPositionId,
                    previousGradeId,
                    positionId,
                    gradeId
            );
        }

        return toFullResponse(saved);
    }

    @Override
    @PreAuthorize("@employeeAccess.canManageEmployeeProfiles()")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
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
    @PreAuthorize("@employeeAccess.canManageEmployeeProfiles()")
    @Transactional
    @CacheEvict(cacheNames = EMPLOYEES, allEntries = true)
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
        applyProfilePhotoUrl(response.getProfilePhoto());
        List<EmployeeResponsibility> responsibilities =
                employeeResponsibilityRepository.findAllByEmployeeId(employee.getId());
        response.setResponsibilities(
                responsibilities.stream()
                        .map(employeeResponsibilityMapper::toResponseDto)
                        .toList()
        );
        return response;
    }

    private void applyProfilePhotoUrl(EmployeeProfilePhotoResponseDto profilePhoto) {
        if (profilePhoto == null || profilePhoto.getObjectKey() == null) {
            return;
        }

        try {
            profilePhoto.setUrl(objectStorageService.createPresignedGetUrl(profilePhoto.getObjectKey()));
        } catch (RuntimeException ex) {
            log.warn("Could not create presigned URL for profile photo of employee {}",
                    profilePhoto.getEmployeeId(), ex);
        }
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

    private void validateSelfUpdateFields(EmployeeUpdateRequestDto request) {
        if (employeeAccessService.isPrivileged()) {
            return;
        }

        if (request.getHireDate() != null
                || request.getPositionId() != null
                || request.getSpecializationId() != null
                || request.getGradeId() != null
                || request.getDepartmentId() != null
                || request.getMaxSubordinates() != null
                || request.getEmploymentStatus() != null) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
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
        requireActive(CatalogNames.POSITION, positionId, position.getActive());
        return position;
    }

    private Grade resolveActiveGrade(UUID gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));
        requireActive(CatalogNames.GRADE, gradeId, grade.getActive());
        return grade;
    }

    private Department resolveActiveDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        requireActive(CatalogNames.DEPARTMENT, departmentId, department.getActive());
        return department;
    }

    private Specialization resolveActiveSpecialization(UUID specializationId) {
        if (specializationId == null) {
            return null;
        }
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException(specializationId));
        requireActive(CatalogNames.SPECIALIZATION, specializationId, specialization.getActive());
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

    private EmployeeLookupItemDto toLookupItem(Employee employee) {
        return new EmployeeLookupItemDto(
                employee.getId(),
                buildFullName(employee),
                employee.getEmploymentStatus() == EmploymentStatus.ACTIVE,
                employee.getPosition() != null ? employee.getPosition().getId() : null,
                employee.getGrade() != null ? employee.getGrade().getId() : null
        );
    }

    private String buildFullName(Employee employee) {
        StringBuilder fullName = new StringBuilder();

        appendPart(fullName, employee.getLastName());
        appendPart(fullName, employee.getFirstName());
        appendPart(fullName, employee.getMiddleName());

        return fullName.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (part == null || part.isBlank()) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(' ');
        }

        builder.append(part.trim());
    }
}
