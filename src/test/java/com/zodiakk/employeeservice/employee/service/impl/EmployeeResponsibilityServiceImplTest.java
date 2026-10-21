package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeResponsibilityCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeResponsibilityUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponsibilityResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.catalog.CatalogInactiveException;
import com.zodiakk.employeeservice.employee.exception.common.InvalidDateRangeException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeTerminatedException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.EmployeeResponsibilityNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.MaxSubordinatesExceededException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.MultipleNotAllowedException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.ResponsibilityAlreadyActiveException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.SelfResponsibilityException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeResponsibilityMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.ResponsibilityTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeResponsibilityServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID RESPONSIBLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID TYPE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID OTHER_TYPE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID RESPONSIBILITY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final UUID OTHER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final String TYPE_CODE = "MANAGER";

    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);

    @Mock
    private EmployeeResponsibilityRepository employeeResponsibilityRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ResponsibilityTypeRepository responsibilityTypeRepository;

    @Mock
    private EmployeeResponsibilityMapper employeeResponsibilityMapper;

    @InjectMocks
    private EmployeeResponsibilityServiceImpl responsibilityService;

    @Nested
    @DisplayName("assign")
    class Assign {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(employeeResponsibilityRepository);
        }

        @Test
        @DisplayName("throws when the employee is terminated")
        void shouldRejectTerminatedEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.TERMINATED, null)));

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(EmployeeTerminatedException.class);
        }

        @Test
        @DisplayName("forbids assigning an employee to themselves")
        void shouldRejectSelfResponsibility() {
            EmployeeResponsibilityCreateRequestDto request = assignRequest();
            request.setResponsibleEmployeeId(EMPLOYEE_ID);

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null)));

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, request))
                    .isInstanceOf(SelfResponsibilityException.class);

            verify(responsibilityTypeRepository, never()).findById(any());
        }

        @Test
        @DisplayName("throws when the responsible employee does not exist")
        void shouldRejectMissingResponsibleEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null)));
            when(employeeRepository.findById(RESPONSIBLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the responsible employee is terminated")
        void shouldRejectTerminatedResponsibleEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null)));
            when(employeeRepository.findById(RESPONSIBLE_ID))
                    .thenReturn(Optional.of(employee(RESPONSIBLE_ID, EmploymentStatus.TERMINATED, null)));

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(EmployeeTerminatedException.class);
        }

        @Test
        @DisplayName("throws when the responsibility type does not exist")
        void shouldRejectMissingResponsibilityType() {
            stubEmployees();
            when(responsibilityTypeRepository.findById(TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(ResponsibilityTypeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the responsibility type is inactive")
        void shouldRejectInactiveResponsibilityType() {
            stubEmployees();
            when(responsibilityTypeRepository.findById(TYPE_ID))
                    .thenReturn(Optional.of(type(TYPE_ID, TYPE_CODE, true, false)));

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(CatalogInactiveException.class);

            verify(employeeResponsibilityRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the end date is before the start date")
        void shouldRejectInvalidDateRange() {
            EmployeeResponsibilityCreateRequestDto request = assignRequest();
            request.setEndDate(START_DATE.minusDays(1));

            stubEmployees();
            when(responsibilityTypeRepository.findById(TYPE_ID))
                    .thenReturn(Optional.of(type(TYPE_ID, TYPE_CODE, true, true)));

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class);

            verifyNoInteractions(employeeResponsibilityMapper);
        }

        @Test
        @DisplayName("throws when an identical responsibility is already active")
        void shouldRejectDuplicateActiveAssignment() {
            stubEmployees();
            when(responsibilityTypeRepository.findById(TYPE_ID))
                    .thenReturn(Optional.of(type(TYPE_ID, TYPE_CODE, true, true)));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, RESPONSIBLE_ID, TYPE_ID))
                    .thenReturn(Optional.of(EmployeeResponsibility.builder().id(RESPONSIBILITY_ID).build()));

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(ResponsibilityAlreadyActiveException.class);
        }

        @Test
        @DisplayName("throws when the type does not allow multiple active assignments")
        void shouldRejectWhenMultipleNotAllowed() {
            stubEmployees();
            when(responsibilityTypeRepository.findById(TYPE_ID))
                    .thenReturn(Optional.of(type(TYPE_ID, TYPE_CODE, null, true)));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, RESPONSIBLE_ID, TYPE_ID))
                    .thenReturn(Optional.empty());
            when(employeeResponsibilityRepository
                    .countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull(
                            RESPONSIBLE_ID, TYPE_CODE))
                    .thenReturn(1L);

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(MultipleNotAllowedException.class);
        }

        @Test
        @DisplayName("throws when the responsible employee has no free subordinate slot")
        void shouldRejectWhenMaxSubordinatesExceeded() {
            stubEmployees(employee(RESPONSIBLE_ID, EmploymentStatus.ACTIVE, 1));
            when(responsibilityTypeRepository.findById(TYPE_ID))
                    .thenReturn(Optional.of(type(TYPE_ID, TYPE_CODE, true, true)));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, RESPONSIBLE_ID, TYPE_ID))
                    .thenReturn(Optional.empty());
            when(employeeResponsibilityRepository
                    .countByResponsibleEmployeeIdAndEndDateIsNull(RESPONSIBLE_ID))
                    .thenReturn(1L);

            assertThatThrownBy(() -> responsibilityService.assign(EMPLOYEE_ID, assignRequest()))
                    .isInstanceOf(MaxSubordinatesExceededException.class);
        }

        @Test
        @DisplayName("assigns a responsibility when the type allows multiple assignments")
        void shouldAssignWhenMultipleAllowed() {
            EmployeeResponsibilityCreateRequestDto request = assignRequest();

            stubEmployees();
            stubSuccessfulAssignmentRules();
            when(employeeResponsibilityMapper.toEntity(request))
                    .thenReturn(EmployeeResponsibility.builder()
                            .startDate(request.getStartDate())
                            .build());
            when(employeeResponsibilityRepository.save(any(EmployeeResponsibility.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            EmployeeResponsibilityResponseDto expected =
                    EmployeeResponsibilityResponseDto.builder().id(RESPONSIBILITY_ID).build();
            when(employeeResponsibilityMapper.toResponseDto(any(EmployeeResponsibility.class)))
                    .thenReturn(expected);

            EmployeeResponsibilityResponseDto actual =
                    responsibilityService.assign(EMPLOYEE_ID, request);

            assertThat(actual).isSameAs(expected);

            ArgumentCaptor<EmployeeResponsibility> captor =
                    ArgumentCaptor.forClass(EmployeeResponsibility.class);
            verify(employeeResponsibilityRepository).save(captor.capture());

            EmployeeResponsibility saved = captor.getValue();
            assertThat(saved.getEmployee().getId()).isEqualTo(EMPLOYEE_ID);
            assertThat(saved.getResponsibleEmployee().getId()).isEqualTo(RESPONSIBLE_ID);
            assertThat(saved.getResponsibilityType().getId()).isEqualTo(TYPE_ID);

            verify(employeeResponsibilityRepository, never())
                    .countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull(any(), any());
        }
    }

    @Nested
    @DisplayName("listByEmployee")
    class ListByEmployee {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.listByEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("maps responsibilities of the employee")
        void shouldReturnResponsibilities() {
            EmployeeResponsibility entity = EmployeeResponsibility.builder().id(RESPONSIBILITY_ID).build();
            EmployeeResponsibilityResponseDto dto =
                    EmployeeResponsibilityResponseDto.builder().id(RESPONSIBILITY_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null)));
            when(employeeResponsibilityRepository.findAllByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(List.of(entity));
            when(employeeResponsibilityMapper.toResponseDto(entity)).thenReturn(dto);

            assertThat(responsibilityService.listByEmployee(EMPLOYEE_ID)).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("listByResponsible")
    class ListByResponsible {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(RESPONSIBLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.listByResponsible(RESPONSIBLE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("maps responsibilities where the employee is responsible")
        void shouldReturnResponsibilities() {
            EmployeeResponsibility entity = EmployeeResponsibility.builder().id(RESPONSIBILITY_ID).build();
            EmployeeResponsibilityResponseDto dto =
                    EmployeeResponsibilityResponseDto.builder().id(RESPONSIBILITY_ID).build();

            when(employeeRepository.findById(RESPONSIBLE_ID))
                    .thenReturn(Optional.of(employee(RESPONSIBLE_ID, EmploymentStatus.ACTIVE, null)));
            when(employeeResponsibilityRepository.findAllByResponsibleEmployeeId(RESPONSIBLE_ID))
                    .thenReturn(List.of(entity));
            when(employeeResponsibilityMapper.toResponseDto(entity)).thenReturn(dto);

            assertThat(responsibilityService.listByResponsible(RESPONSIBLE_ID)).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("throws when the responsibility does not exist")
        void shouldRejectMissingResponsibility() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.close(RESPONSIBILITY_ID, null))
                    .isInstanceOf(EmployeeResponsibilityNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the closing date is before the start date")
        void shouldRejectInvalidClosingDate() {
            EmployeeResponsibility responsibility = activeResponsibility();

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));

            assertThatThrownBy(() -> responsibilityService.close(
                    RESPONSIBILITY_ID,
                    START_DATE.minusDays(1)
            )).isInstanceOf(InvalidDateRangeException.class);

            verify(employeeResponsibilityRepository, never()).save(any());
        }

        @Test
        @DisplayName("closes the responsibility with today's date by default")
        void shouldCloseWithTodayWhenDateIsMissing() {
            EmployeeResponsibility responsibility = activeResponsibility();
            EmployeeResponsibilityResponseDto expected =
                    EmployeeResponsibilityResponseDto.builder().id(RESPONSIBILITY_ID).build();

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));
            when(employeeResponsibilityRepository.save(responsibility)).thenReturn(responsibility);
            when(employeeResponsibilityMapper.toResponseDto(responsibility)).thenReturn(expected);

            EmployeeResponsibilityResponseDto actual =
                    responsibilityService.close(RESPONSIBILITY_ID, null);

            assertThat(actual).isSameAs(expected);
            assertThat(responsibility.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("closes the responsibility with the supplied date")
        void shouldCloseWithSuppliedDate() {
            EmployeeResponsibility responsibility = activeResponsibility();
            LocalDate endDate = START_DATE.plusMonths(3);

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));
            when(employeeResponsibilityRepository.save(responsibility)).thenReturn(responsibility);
            when(employeeResponsibilityMapper.toResponseDto(responsibility))
                    .thenReturn(EmployeeResponsibilityResponseDto.builder().build());

            responsibilityService.close(RESPONSIBILITY_ID, endDate);

            assertThat(responsibility.getEndDate()).isEqualTo(endDate);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when the responsibility does not exist")
        void shouldRejectMissingResponsibility() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder().build()
            )).isInstanceOf(EmployeeResponsibilityNotFoundException.class);
        }

        @Test
        @DisplayName("forbids making the employee responsible for themselves")
        void shouldRejectSelfResponsibility() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null)));

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibleEmployeeId(EMPLOYEE_ID)
                            .build()
            )).isInstanceOf(SelfResponsibilityException.class);
        }

        @Test
        @DisplayName("throws when the new responsible employee does not exist")
        void shouldRejectMissingResponsibleEmployee() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));
            when(employeeRepository.findById(OTHER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibleEmployeeId(OTHER_ID)
                            .build()
            )).isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the new responsible employee is terminated")
        void shouldRejectTerminatedResponsibleEmployee() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));
            when(employeeRepository.findById(OTHER_ID))
                    .thenReturn(Optional.of(employee(OTHER_ID, EmploymentStatus.TERMINATED, null)));

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibleEmployeeId(OTHER_ID)
                            .build()
            )).isInstanceOf(EmployeeTerminatedException.class);
        }

        @Test
        @DisplayName("throws when the new responsibility type does not exist")
        void shouldRejectMissingResponsibilityType() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));
            when(responsibilityTypeRepository.findById(OTHER_TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibilityTypeId(OTHER_TYPE_ID)
                            .build()
            )).isInstanceOf(ResponsibilityTypeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the new responsibility type is inactive")
        void shouldRejectInactiveResponsibilityType() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));
            when(responsibilityTypeRepository.findById(OTHER_TYPE_ID))
                    .thenReturn(Optional.of(type(OTHER_TYPE_ID, "OTHER", true, false)));

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibilityTypeId(OTHER_TYPE_ID)
                            .build()
            )).isInstanceOf(CatalogInactiveException.class);
        }

        @Test
        @DisplayName("throws when the resulting date range is invalid")
        void shouldRejectInvalidDateRange() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .startDate(START_DATE.plusMonths(2))
                            .endDate(START_DATE.plusMonths(1))
                            .build()
            )).isInstanceOf(InvalidDateRangeException.class);

            verify(employeeResponsibilityRepository, never()).save(any());
        }

        @Test
        @DisplayName("updates a closed responsibility without revalidating the assignment")
        void shouldSkipValidationForClosedResponsibility() {
            EmployeeResponsibility responsibility = activeResponsibility();
            responsibility.setEndDate(START_DATE.plusMonths(1));

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));
            when(employeeResponsibilityRepository.save(responsibility)).thenReturn(responsibility);
            when(employeeResponsibilityMapper.toResponseDto(responsibility))
                    .thenReturn(EmployeeResponsibilityResponseDto.builder().build());

            responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder().build()
            );

            verify(employeeResponsibilityRepository, never())
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            any(), any(), any());
            verify(employeeResponsibilityMapper).updateEntity(
                    any(EmployeeResponsibilityUpdateRequestDto.class),
                    any(EmployeeResponsibility.class)
            );
        }

        @Test
        @DisplayName("revalidates the assignment when the responsible employee changes")
        void shouldRevalidateWhenResponsibleChanges() {
            EmployeeResponsibility responsibility = activeResponsibility();
            Employee newResponsible = employee(OTHER_ID, EmploymentStatus.ACTIVE, null);

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));
            when(employeeRepository.findById(OTHER_ID)).thenReturn(Optional.of(newResponsible));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, OTHER_ID, TYPE_ID))
                    .thenReturn(Optional.empty());
            when(employeeResponsibilityRepository.save(responsibility)).thenReturn(responsibility);
            when(employeeResponsibilityMapper.toResponseDto(responsibility))
                    .thenReturn(EmployeeResponsibilityResponseDto.builder().build());

            responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibleEmployeeId(OTHER_ID)
                            .build()
            );

            assertThat(responsibility.getResponsibleEmployee().getId()).isEqualTo(OTHER_ID);
        }

        @Test
        @DisplayName("excludes the responsibility itself from the multiple and subordinate counters")
        void shouldExcludeCurrentResponsibilityFromCounters() {
            EmployeeResponsibility responsibility = activeResponsibility(1);
            ResponsibilityType replacement = type(OTHER_TYPE_ID, TYPE_CODE, false, true);

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));
            when(responsibilityTypeRepository.findById(OTHER_TYPE_ID)).thenReturn(Optional.of(replacement));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, RESPONSIBLE_ID, OTHER_TYPE_ID))
                    .thenReturn(Optional.empty());
            when(employeeResponsibilityRepository
                    .countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull(
                            RESPONSIBLE_ID, TYPE_CODE))
                    .thenReturn(1L);
            when(employeeResponsibilityRepository
                    .countByResponsibleEmployeeIdAndEndDateIsNull(RESPONSIBLE_ID))
                    .thenReturn(1L);
            when(employeeResponsibilityRepository.save(responsibility)).thenReturn(responsibility);
            when(employeeResponsibilityMapper.toResponseDto(responsibility))
                    .thenReturn(EmployeeResponsibilityResponseDto.builder().build());

            responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibilityTypeId(OTHER_TYPE_ID)
                            .build()
            );

            assertThat(responsibility.getResponsibilityType().getId()).isEqualTo(OTHER_TYPE_ID);
        }

        @Test
        @DisplayName("throws when another active assignment of a single-use type exists")
        void shouldRejectWhenMultipleNotAllowedOnUpdate() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility()));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, RESPONSIBLE_ID, OTHER_TYPE_ID))
                    .thenReturn(Optional.empty());
            when(responsibilityTypeRepository.findById(OTHER_TYPE_ID))
                    .thenReturn(Optional.of(type(OTHER_TYPE_ID, "OTHER", false, true)));
            when(employeeResponsibilityRepository
                    .countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull(
                            RESPONSIBLE_ID, "OTHER"))
                    .thenReturn(1L);

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibilityTypeId(OTHER_TYPE_ID)
                            .build()
            )).isInstanceOf(MultipleNotAllowedException.class);
        }

        @Test
        @DisplayName("throws when the responsible employee has no free subordinate slot")
        void shouldRejectWhenMaxSubordinatesExceededOnUpdate() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(activeResponsibility(1)));
            when(employeeResponsibilityRepository
                    .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                            EMPLOYEE_ID, RESPONSIBLE_ID, OTHER_TYPE_ID))
                    .thenReturn(Optional.empty());
            when(responsibilityTypeRepository.findById(OTHER_TYPE_ID))
                    .thenReturn(Optional.of(type(OTHER_TYPE_ID, "OTHER", true, true)));
            when(employeeResponsibilityRepository
                    .countByResponsibleEmployeeIdAndEndDateIsNull(RESPONSIBLE_ID))
                    .thenReturn(2L);

            assertThatThrownBy(() -> responsibilityService.update(
                    RESPONSIBILITY_ID,
                    EmployeeResponsibilityUpdateRequestDto.builder()
                            .responsibilityTypeId(OTHER_TYPE_ID)
                            .build()
            )).isInstanceOf(MaxSubordinatesExceededException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when the responsibility does not exist")
        void shouldRejectMissingResponsibility() {
            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityService.delete(RESPONSIBILITY_ID))
                    .isInstanceOf(EmployeeResponsibilityNotFoundException.class);

            verify(employeeResponsibilityRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes the responsibility")
        void shouldDeleteResponsibility() {
            EmployeeResponsibility responsibility = activeResponsibility();

            when(employeeResponsibilityRepository.findById(RESPONSIBILITY_ID))
                    .thenReturn(Optional.of(responsibility));

            assertThatCode(() -> responsibilityService.delete(RESPONSIBILITY_ID))
                    .doesNotThrowAnyException();

            verify(employeeResponsibilityRepository).delete(responsibility);
        }
    }

    private static EmployeeResponsibilityCreateRequestDto assignRequest() {
        return EmployeeResponsibilityCreateRequestDto.builder()
                .responsibleEmployeeId(RESPONSIBLE_ID)
                .responsibilityTypeId(TYPE_ID)
                .startDate(START_DATE)
                .build();
    }

    private static Employee employee(UUID id, EmploymentStatus status, Integer maxSubordinates) {
        return Employee.builder()
                .id(id)
                .firstName("Ivan")
                .lastName("Petrov")
                .email(id + "@company.com")
                .hireDate(LocalDate.of(2024, 1, 1))
                .employmentStatus(status)
                .maxSubordinates(maxSubordinates)
                .build();
    }

    private static ResponsibilityType type(
            UUID id,
            String code,
            Boolean multipleAllowed,
            boolean active
    ) {
        return ResponsibilityType.builder()
                .id(id)
                .code(code)
                .name("Type " + code)
                .multipleAllowed(multipleAllowed)
                .active(active)
                .build();
    }

    private static EmployeeResponsibility activeResponsibility() {
        return activeResponsibility(5);
    }

    private static EmployeeResponsibility activeResponsibility(int responsibleMaxSubordinates) {
        Employee employee = employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null);
        Employee responsible = employee(RESPONSIBLE_ID, EmploymentStatus.ACTIVE, responsibleMaxSubordinates);

        return EmployeeResponsibility.builder()
                .id(RESPONSIBILITY_ID)
                .employee(employee)
                .responsibleEmployee(responsible)
                .responsibilityType(type(TYPE_ID, TYPE_CODE, true, true))
                .startDate(START_DATE)
                .build();
    }

    private void stubEmployees() {
        stubEmployees(employee(RESPONSIBLE_ID, EmploymentStatus.ACTIVE, 5));
    }

    private void stubEmployees(Employee responsible) {
        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee(EMPLOYEE_ID, EmploymentStatus.ACTIVE, null)));
        when(employeeRepository.findById(RESPONSIBLE_ID)).thenReturn(Optional.of(responsible));
    }

    private void stubSuccessfulAssignmentRules() {
        when(responsibilityTypeRepository.findById(TYPE_ID))
                .thenReturn(Optional.of(type(TYPE_ID, TYPE_CODE, true, true)));
        when(employeeResponsibilityRepository
                .findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull(
                        EMPLOYEE_ID, RESPONSIBLE_ID, TYPE_ID))
                .thenReturn(Optional.empty());
        when(employeeResponsibilityRepository
                .countByResponsibleEmployeeIdAndEndDateIsNull(RESPONSIBLE_ID))
                .thenReturn(0L);
    }
}
