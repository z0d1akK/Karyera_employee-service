package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.storage.ObjectStorageService;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.EmployeeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeRegistrationInfoDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponsibilityResponseDto;
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
import com.zodiakk.employeeservice.integration.employee.service.EmployeeEventService;
import com.zodiakk.employeeservice.security.access.EmployeeAccessService;
import com.zodiakk.employeeservice.security.principal.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID POSITION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID GRADE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID DEPARTMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final UUID SPECIALIZATION_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final UUID OTHER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    private static final String EMAIL = "ivan.petrov@company.com";

    private static final String RAW_EMAIL = "  Ivan.Petrov@Company.COM  ";

    private static final String PHOTO_OBJECT_KEY = "employees/photo/avatar.jpg";

    private static final LocalDate HIRE_DATE = LocalDate.of(2024, 8, 1);

    private static final LocalDate BIRTH_DATE = LocalDate.of(1995, 5, 15);

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private EmployeeResponsibilityRepository employeeResponsibilityRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private EmployeeResponsibilityMapper employeeResponsibilityMapper;

    @Mock
    private EmployeeAccessService employeeAccessService;

    @Mock
    private EmployeeEventService employeeEventService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void stubMapper() {
        lenient().when(employeeMapper.toEntity(any(EmployeeCreateRequestDto.class)))
                .thenReturn(Employee.builder().build());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("rejects a userId that is already linked to an employee")
        void shouldRejectDuplicateUserId() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            when(employeeRepository.existsByUserId(USER_ID)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(EmployeeAlreadyExistsException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
            verifyNoInteractions(positionRepository);
        }

        @Test
        @DisplayName("rejects a duplicated email")
        void shouldRejectDuplicateEmail() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            when(employeeRepository.existsByUserId(USER_ID)).thenReturn(false);
            when(employeeRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(EmployeeAlreadyExistsException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("rejects an employee created with TERMINATED status")
        void shouldRejectTerminatedStatus() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.TERMINATED);

            stubNoDuplicates(USER_ID, EMAIL);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(EmployeeCannotBeCreatedTerminatedException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
            verifyNoInteractions(employeeMapper);
        }

        @Test
        @DisplayName("defaults a missing employment status to ACTIVE and rejects a future hire date")
        void shouldRejectFutureHireDate() {
            EmployeeCreateRequestDto request = createRequest(null);
            request.setHireDate(LocalDate.now().plusDays(1));

            stubNoDuplicates(USER_ID, EMAIL);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(InvalidEmployeeDatesException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("rejects a birth date that is not before the hire date")
        void shouldRejectBirthDateNotBeforeHireDate() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);
            request.setBirthDate(HIRE_DATE);

            stubNoDuplicates(USER_ID, EMAIL);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(InvalidEmployeeDatesException.class);
        }

        @Test
        @DisplayName("rejects an unknown position")
        void shouldRejectUnknownPosition() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(PositionNotFoundException.class);
        }

        @Test
        @DisplayName("rejects an inactive position")
        void shouldRejectInactivePosition() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, false)));

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(CatalogInactiveException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("rejects an unknown grade")
        void shouldRejectUnknownGrade() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(GradeNotFoundException.class);
        }

        @Test
        @DisplayName("rejects an inactive grade")
        void shouldRejectInactiveGrade() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, false)));

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(CatalogInactiveException.class);
        }

        @Test
        @DisplayName("rejects an unknown department")
        void shouldRejectUnknownDepartment() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, true)));
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(DepartmentNotFoundException.class);
        }

        @Test
        @DisplayName("rejects an inactive department")
        void shouldRejectInactiveDepartment() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, true)));
            when(departmentRepository.findById(DEPARTMENT_ID))
                    .thenReturn(Optional.of(department(DEPARTMENT_ID, false)));

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(CatalogInactiveException.class);
        }

        @Test
        @DisplayName("rejects an unknown specialization")
        void shouldRejectUnknownSpecialization() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, true)));
            when(departmentRepository.findById(DEPARTMENT_ID))
                    .thenReturn(Optional.of(department(DEPARTMENT_ID, true)));
            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(SpecializationNotFoundException.class);
        }

        @Test
        @DisplayName("rejects an inactive specialization")
        void shouldRejectInactiveSpecialization() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);

            stubNoDuplicates(USER_ID, EMAIL);
            stubEmployeeEntity(request);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, true)));
            when(departmentRepository.findById(DEPARTMENT_ID))
                    .thenReturn(Optional.of(department(DEPARTMENT_ID, true)));
            when(specializationRepository.findById(SPECIALIZATION_ID))
                    .thenReturn(Optional.of(specialization(SPECIALIZATION_ID, false)));

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(CatalogInactiveException.class);
        }

        @Test
        @DisplayName("creates an employee, normalizing the email and resolving active catalogs")
        void shouldCreateEmployee() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ACTIVE);
            request.setEmail(RAW_EMAIL);

            stubNoDuplicates(USER_ID, RAW_EMAIL);
            stubActiveCatalogs();
            when(employeeMapper.toEntity(request)).thenReturn(Employee.builder().build());
            EmployeeResponseDto expected = stubSavedEmployeeResponse();

            EmployeeResponseDto actual = employeeService.create(request);

            assertThat(actual).isSameAs(expected);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());

            Employee saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getEmploymentStatus()).isEqualTo(EmploymentStatus.ACTIVE);
            assertThat(saved.getPosition().getId()).isEqualTo(POSITION_ID);
            assertThat(saved.getGrade().getId()).isEqualTo(GRADE_ID);
            assertThat(saved.getDepartment().getId()).isEqualTo(DEPARTMENT_ID);
            assertThat(saved.getSpecialization().getId()).isEqualTo(SPECIALIZATION_ID);
        }

        @Test
        @DisplayName("defaults a missing employment status to ACTIVE")
        void shouldDefaultEmploymentStatusToActive() {
            EmployeeCreateRequestDto request = createRequest(null);

            stubNoDuplicates(USER_ID, EMAIL);
            stubActiveCatalogs();
            when(employeeMapper.toEntity(request)).thenReturn(Employee.builder().build());
            stubSavedEmployeeResponse();

            employeeService.create(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());

            assertThat(captor.getValue().getEmploymentStatus()).isEqualTo(EmploymentStatus.ACTIVE);
        }

        @Test
        @DisplayName("skips date validation without a hire date and accepts absent optional catalogs")
        void shouldAllowMissingHireDateAndOptionalCatalogs() {
            EmployeeCreateRequestDto request = createRequest(EmploymentStatus.ON_LEAVE);
            request.setHireDate(null);
            request.setBirthDate(null);
            request.setDepartmentId(null);
            request.setSpecializationId(null);

            stubNoDuplicates(USER_ID, EMAIL);
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, true)));
            when(employeeMapper.toEntity(request)).thenReturn(Employee.builder().build());
            stubSavedEmployeeResponse();

            employeeService.create(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());

            Employee saved = captor.getValue();
            assertThat(saved.getDepartment()).isNull();
            assertThat(saved.getSpecialization()).isNull();
            assertThat(saved.getEmploymentStatus()).isEqualTo(EmploymentStatus.ON_LEAVE);
            verifyNoInteractions(departmentRepository, specializationRepository);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getById(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("maps the employee together with its responsibilities")
        void shouldReturnEmployeeWithResponsibilities() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeResponsibility responsibility = EmployeeResponsibility.builder().id(OTHER_ID).build();
            EmployeeResponsibilityResponseDto responsibilityDto =
                    EmployeeResponsibilityResponseDto.builder().id(OTHER_ID).build();
            EmployeeResponseDto response = EmployeeResponseDto.builder().id(EMPLOYEE_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeMapper.toResponseDto(employee)).thenReturn(response);
            when(employeeResponsibilityRepository.findAllByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(List.of(responsibility));
            when(employeeResponsibilityMapper.toResponseDto(responsibility)).thenReturn(responsibilityDto);

            EmployeeResponseDto actual = employeeService.getById(EMPLOYEE_ID);

            assertThat(actual.getResponsibilities()).containsExactly(responsibilityDto);
        }
    }

    @Nested
    @DisplayName("getByUserId")
    class GetByUserId {

        @Test
        @DisplayName("throws when no employee is linked to the user")
        void shouldThrowWhenMissing() {
            when(employeeRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getByUserId(USER_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("returns the employee linked to the user")
        void shouldReturnEmployee() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeResponseDto response = EmployeeResponseDto.builder().id(EMPLOYEE_ID).userId(USER_ID).build();

            when(employeeRepository.findByUserId(USER_ID)).thenReturn(Optional.of(employee));
            when(employeeMapper.toResponseDto(employee)).thenReturn(response);
            when(employeeResponsibilityRepository.findAllByEmployeeId(EMPLOYEE_ID)).thenReturn(List.of());

            assertThat(employeeService.getByUserId(USER_ID).getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("getRegistrationInfo")
    class GetRegistrationInfo {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getRegistrationInfo(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("returns the registration projection")
        void shouldReturnRegistrationInfo() {
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));

            EmployeeRegistrationInfoDto info = employeeService.getRegistrationInfo(EMPLOYEE_ID);

            assertThat(info.employeeId()).isEqualTo(EMPLOYEE_ID);
            assertThat(info.userId()).isEqualTo(USER_ID);
            assertThat(info.email()).isEqualTo(EMAIL);
            assertThat(info.employmentStatus()).isEqualTo(EmploymentStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns a mapped page without manager restrictions")
        void shouldReturnMappedPage() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeSummaryResponseDto summary = EmployeeSummaryResponseDto.builder().id(EMPLOYEE_ID).build();

            when(employeeAccessService.isManagerOnly()).thenReturn(false);
            when(employeeRepository.findAll(
                    org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(employee)));
            when(employeeMapper.toSummaryResponseDto(employee)).thenReturn(summary);

            Page<EmployeeSummaryResponseDto> result = employeeService.findAll(
                    EmployeeFilterDto.builder().firstName("Ivan").build(),
                    PageRequestDto.builder().build()
            );

            assertThat(result.getContent()).containsExactly(summary);
            verifyNoInteractions(objectStorageService);
        }

        @Test
        @DisplayName("returns an empty page for a null filter")
        void shouldHandleNullFilter() {
            when(employeeAccessService.isManagerOnly()).thenReturn(false);
            when(employeeRepository.findAll(
                    org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of()));

            Page<EmployeeSummaryResponseDto> result = employeeService.findAll(
                    null,
                    PageRequestDto.builder().build()
            );

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("restricts the result to the subordinates of a manager")
        void shouldRestrictToSubordinatesForManager() {
            authenticate("ROLE_MANAGER");
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeSummaryResponseDto summary = EmployeeSummaryResponseDto.builder().id(EMPLOYEE_ID).build();

            when(employeeAccessService.isManagerOnly()).thenReturn(true);
            when(employeeRepository.findAll(
                    org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(employee)));
            when(employeeMapper.toSummaryResponseDto(employee)).thenReturn(summary);

            Page<EmployeeSummaryResponseDto> result = employeeService.findAll(
                    EmployeeFilterDto.builder().build(),
                    PageRequestDto.builder().build()
            );

            assertThat(result.getContent()).containsExactly(summary);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, updateRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(employeeAccessService);
        }

        @ParameterizedTest(name = "rejects a self update of {0}")
        @MethodSource("com.zodiakk.employeeservice.employee.service.impl.EmployeeServiceImplTest#forbiddenSelfUpdates")
        @DisplayName("forbids non-privileged changes to protected fields")
        void shouldRejectRestrictedSelfUpdate(String field, EmployeeUpdateRequestDto request) {
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(false);

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(AccessDeniedException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("allows a non-privileged employee to change their own names")
        void shouldAllowSelfUpdateOfNames() {
            Employee employee = employee(EmploymentStatus.ACTIVE);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeAccessService.isPrivileged()).thenReturn(false);
            doAnswer(invocation -> {
                EmployeeUpdateRequestDto dto = invocation.getArgument(0);
                Employee target = invocation.getArgument(1);
                target.setFirstName(dto.getFirstName());
                return null;
            }).when(employeeMapper).updateEntity(any(EmployeeUpdateRequestDto.class), any(Employee.class));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.update(EMPLOYEE_ID, updateRequest());

            assertThat(employee.getFirstName()).isEqualTo("Petr");

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());
            assertThat(captor.getValue().getPosition().getId()).isEqualTo(POSITION_ID);
        }

        @Test
        @DisplayName("resolves all catalogs for a privileged update")
        void shouldResolveCatalogsForPrivilegedUpdate() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder()
                    .positionId(OTHER_ID)
                    .gradeId(OTHER_ID)
                    .departmentId(OTHER_ID)
                    .specializationId(OTHER_ID)
                    .hireDate(LocalDate.of(2023, 1, 10))
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(positionRepository.findById(OTHER_ID))
                    .thenReturn(Optional.of(position(OTHER_ID, true)));
            when(gradeRepository.findById(OTHER_ID)).thenReturn(Optional.of(grade(OTHER_ID, true)));
            when(departmentRepository.findById(OTHER_ID))
                    .thenReturn(Optional.of(department(OTHER_ID, true)));
            when(specializationRepository.findById(OTHER_ID))
                    .thenReturn(Optional.of(specialization(OTHER_ID, true)));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.update(EMPLOYEE_ID, request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());

            Employee saved = captor.getValue();
            assertThat(saved.getPosition().getId()).isEqualTo(OTHER_ID);
            assertThat(saved.getGrade().getId()).isEqualTo(OTHER_ID);
            assertThat(saved.getDepartment().getId()).isEqualTo(OTHER_ID);
            assertThat(saved.getSpecialization().getId()).isEqualTo(OTHER_ID);

            verify(employeeEventService).recordPositionChanged(
                    EMPLOYEE_ID,
                    POSITION_ID,
                    GRADE_ID,
                    OTHER_ID,
                    OTHER_ID
            );
        }

        @Test
        @DisplayName("rejects an unknown position while updating")
        void shouldRejectUnknownPosition() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().positionId(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(positionRepository.findById(OTHER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(PositionNotFoundException.class);
        }

        @Test
        @DisplayName("rejects an inactive grade while updating")
        void shouldRejectInactiveGrade() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().gradeId(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(gradeRepository.findById(OTHER_ID)).thenReturn(Optional.of(grade(OTHER_ID, false)));

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(CatalogInactiveException.class);
        }

        @Test
        @DisplayName("rejects an unknown department while updating")
        void shouldRejectUnknownDepartment() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().departmentId(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(departmentRepository.findById(OTHER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(DepartmentNotFoundException.class);
        }

        @Test
        @DisplayName("rejects an inactive specialization while updating")
        void shouldRejectInactiveSpecialization() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().specializationId(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(specializationRepository.findById(OTHER_ID))
                    .thenReturn(Optional.of(specialization(OTHER_ID, false)));

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(CatalogInactiveException.class);
        }

        @Test
        @DisplayName("closes active responsibilities when the employee becomes terminated")
        void shouldCloseResponsibilitiesOnTermination() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeResponsibility open = EmployeeResponsibility.builder()
                    .id(OTHER_ID)
                    .startDate(HIRE_DATE)
                    .build();
            EmployeeResponsibility alreadyClosed = EmployeeResponsibility.builder()
                    .id(DEPARTMENT_ID)
                    .startDate(HIRE_DATE)
                    .endDate(HIRE_DATE)
                    .build();
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder()
                    .employmentStatus(EmploymentStatus.TERMINATED)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(employeeResponsibilityRepository.findActiveInvolvingEmployee(EMPLOYEE_ID))
                    .thenReturn(List.of(open, alreadyClosed));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.update(EMPLOYEE_ID, request);

            assertThat(open.getEndDate()).isEqualTo(LocalDate.now());
            assertThat(alreadyClosed.getEndDate()).isEqualTo(HIRE_DATE);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<EmployeeResponsibility>> captor = ArgumentCaptor.forClass(List.class);
            verify(employeeResponsibilityRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("does not close responsibilities when the employee is already terminated")
        void shouldNotCloseResponsibilitiesWhenAlreadyTerminated() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder()
                    .employmentStatus(EmploymentStatus.TERMINATED)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.TERMINATED)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.update(EMPLOYEE_ID, request);

            verify(employeeResponsibilityRepository, never()).findActiveInvolvingEmployee(any());
            verify(employeeResponsibilityRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("rejects an email already used by another employee")
        void shouldRejectDuplicateEmail() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().email("other@company.com").build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("other@company.com", EMPLOYEE_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(EmployeeAlreadyExistsException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("normalizes the updated email")
        void shouldNormalizeUpdatedEmail() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().email(RAW_EMAIL).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeAccessService.isPrivileged()).thenReturn(true);
            when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(RAW_EMAIL, EMPLOYEE_ID)).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.update(EMPLOYEE_ID, request);

            assertThat(employee.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("rejects invalid dates while updating")
        void shouldRejectInvalidDates() {
            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder().birthDate(HIRE_DATE).build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));
            when(employeeAccessService.isPrivileged()).thenReturn(true);

            assertThatThrownBy(() -> employeeService.update(EMPLOYEE_ID, request))
                    .isInstanceOf(InvalidEmployeeDatesException.class);

            verify(employeeMapper, never()).updateEntity(any(), any());
        }
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.changeStatus(EMPLOYEE_ID, EmploymentStatus.ACTIVE))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("closes responsibilities when the employee becomes terminated")
        void shouldCloseResponsibilitiesOnTermination() {
            Employee employee = employee(EmploymentStatus.ACTIVE);
            EmployeeResponsibility open = EmployeeResponsibility.builder().id(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeResponsibilityRepository.findActiveInvolvingEmployee(EMPLOYEE_ID))
                    .thenReturn(List.of(open));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.changeStatus(EMPLOYEE_ID, EmploymentStatus.TERMINATED);

            assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.TERMINATED);
            assertThat(open.getEndDate()).isEqualTo(LocalDate.now());
            verify(employeeResponsibilityRepository).saveAll(any());
        }

        @Test
        @DisplayName("does not close responsibilities when the employee is already terminated")
        void shouldNotCloseWhenAlreadyTerminated() {
            Employee employee = employee(EmploymentStatus.TERMINATED);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.changeStatus(EMPLOYEE_ID, EmploymentStatus.TERMINATED);

            verify(employeeResponsibilityRepository, never()).findActiveInvolvingEmployee(any());
        }

        @Test
        @DisplayName("changes status without closing responsibilities for a non-terminated status")
        void shouldChangeToNonTerminatedStatus() {
            Employee employee = employee(EmploymentStatus.ACTIVE);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
            stubFullResponse();

            employeeService.changeStatus(EMPLOYEE_ID, EmploymentStatus.ON_LEAVE);

            assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ON_LEAVE);
            verify(employeeResponsibilityRepository, never()).findActiveInvolvingEmployee(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.delete(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("refuses to delete an employee that is not terminated")
        void shouldRejectNonTerminatedEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EmploymentStatus.ACTIVE)));

            assertThatThrownBy(() -> employeeService.delete(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotTerminatedException.class);

            verifyNoInteractions(objectStorageService);
            verify(employeeRepository, never()).delete(any(Employee.class));
        }

        @Test
        @DisplayName("deletes the stored photo, responsibilities and the employee")
        void shouldDeleteTerminatedEmployee() {
            Employee employee = employee(EmploymentStatus.TERMINATED);
            employee.setProfilePhoto(EmployeeProfilePhoto.builder().objectKey(PHOTO_OBJECT_KEY).build());

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThatCode(() -> employeeService.delete(EMPLOYEE_ID)).doesNotThrowAnyException();

            verify(objectStorageService).delete(PHOTO_OBJECT_KEY);
            verify(employeeResponsibilityRepository)
                    .deleteByEmployeeIdOrResponsibleEmployeeId(EMPLOYEE_ID, EMPLOYEE_ID);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("deletes an employee without a photo")
        void shouldDeleteWithoutPhoto() {
            Employee employee = employee(EmploymentStatus.TERMINATED);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            employeeService.delete(EMPLOYEE_ID);

            verifyNoInteractions(objectStorageService);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("skips storage deletion when the photo has no object key")
        void shouldSkipStorageDeletionWhenObjectKeyIsMissing() {
            Employee employee = employee(EmploymentStatus.TERMINATED);
            employee.setProfilePhoto(EmployeeProfilePhoto.builder().build());

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            employeeService.delete(EMPLOYEE_ID);

            verifyNoInteractions(objectStorageService);
            verify(employeeRepository).delete(employee);
        }
    }

    static Stream<Arguments> forbiddenSelfUpdates() {
        return Stream.of(
                Arguments.of("hire date", EmployeeUpdateRequestDto.builder()
                        .hireDate(LocalDate.of(2023, 2, 1)).build()),
                Arguments.of("position", EmployeeUpdateRequestDto.builder()
                        .positionId(OTHER_ID).build()),
                Arguments.of("specialization", EmployeeUpdateRequestDto.builder()
                        .specializationId(OTHER_ID).build()),
                Arguments.of("grade", EmployeeUpdateRequestDto.builder()
                        .gradeId(OTHER_ID).build()),
                Arguments.of("department", EmployeeUpdateRequestDto.builder()
                        .departmentId(OTHER_ID).build()),
                Arguments.of("max subordinates", EmployeeUpdateRequestDto.builder()
                        .maxSubordinates(3).build()),
                Arguments.of("employment status", EmployeeUpdateRequestDto.builder()
                        .employmentStatus(EmploymentStatus.ON_LEAVE).build())
        );
    }

    private static EmployeeCreateRequestDto createRequest(EmploymentStatus status) {
        return EmployeeCreateRequestDto.builder()
                .userId(USER_ID)
                .firstName("Ivan")
                .lastName("Petrov")
                .email(EMAIL)
                .birthDate(BIRTH_DATE)
                .hireDate(HIRE_DATE)
                .positionId(POSITION_ID)
                .gradeId(GRADE_ID)
                .departmentId(DEPARTMENT_ID)
                .specializationId(SPECIALIZATION_ID)
                .maxSubordinates(5)
                .employmentStatus(status)
                .build();
    }

    private static EmployeeUpdateRequestDto updateRequest() {
        return EmployeeUpdateRequestDto.builder().firstName("Petr").build();
    }

    private static Employee employee(EmploymentStatus status) {
        return Employee.builder()
                .id(EMPLOYEE_ID)
                .userId(USER_ID)
                .firstName("Ivan")
                .lastName("Petrov")
                .middleName("Ivanovich")
                .email(EMAIL)
                .birthDate(BIRTH_DATE)
                .hireDate(HIRE_DATE)
                .position(position(POSITION_ID, true))
                .grade(grade(GRADE_ID, true))
                .department(department(DEPARTMENT_ID, true))
                .specialization(specialization(SPECIALIZATION_ID, true))
                .employmentStatus(status)
                .build();
    }

    private static Position position(UUID id, boolean active) {
        return Position.builder().id(id).name("Engineer").active(active).build();
    }

    private static Grade grade(UUID id, boolean active) {
        return Grade.builder().id(id).name("Senior").level(3).active(active).build();
    }

    private static Department department(UUID id, boolean active) {
        return Department.builder().id(id).name("Engineering").active(active).build();
    }

    private static Specialization specialization(UUID id, boolean active) {
        return Specialization.builder().id(id).name("Backend").active(active).build();
    }

    private void stubNoDuplicates(UUID userId, String email) {
        when(employeeRepository.existsByUserId(userId)).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
    }

    private void stubEmployeeEntity(EmployeeCreateRequestDto request) {
        when(employeeMapper.toEntity(request)).thenReturn(Employee.builder().build());
    }

    private void stubActiveCatalogs() {
        when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(position(POSITION_ID, true)));
        when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(grade(GRADE_ID, true)));
        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(department(DEPARTMENT_ID, true)));
        when(specializationRepository.findById(SPECIALIZATION_ID))
                .thenReturn(Optional.of(specialization(SPECIALIZATION_ID, true)));
    }

    private EmployeeResponseDto stubSavedEmployeeResponse() {
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(EMPLOYEE_ID);
            }
            return saved;
        });

        return stubFullResponse();
    }

    private EmployeeResponseDto stubFullResponse() {
        EmployeeResponseDto response = EmployeeResponseDto.builder().id(EMPLOYEE_ID).build();

        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(response);
        when(employeeResponsibilityRepository.findAllByEmployeeId(eq(EMPLOYEE_ID))).thenReturn(List.of());

        return response;
    }

    private void authenticate(String role) {
        CurrentUser currentUser = CurrentUser.builder()
                .userId(USER_ID)
                .role(role)
                .email(EMAIL)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of())
        );
    }
}
