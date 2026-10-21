package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.MilitaryObligationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.MilitaryObligationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.MilitaryObligationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.MilitaryObligation;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.entity.enums.MilitaryStatus;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.military.MilitaryObligationAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.military.MilitaryObligationNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.MilitaryObligationMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.MilitaryObligationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
class MilitaryObligationServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID OBLIGATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private MilitaryObligationRepository militaryObligationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private MilitaryObligationMapper militaryObligationMapper;

    @InjectMocks
    private MilitaryObligationServiceImpl militaryObligationService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.create(EMPLOYEE_ID, createRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(militaryObligationRepository, militaryObligationMapper);
        }

        @Test
        @DisplayName("throws when the employee already has a military obligation")
        void shouldRejectExistingObligation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(militaryObligationRepository.existsByEmployeeId(EMPLOYEE_ID)).thenReturn(true);

            assertThatThrownBy(() -> militaryObligationService.create(EMPLOYEE_ID, createRequest()))
                    .isInstanceOf(MilitaryObligationAlreadyExistsException.class);

            verify(militaryObligationRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates a military obligation and attaches it to the employee")
        void shouldCreateObligation() {
            Employee employee = employee();
            MilitaryObligationCreateRequestDto request = createRequest();
            MilitaryObligation obligation = MilitaryObligation.builder()
                    .id(OBLIGATION_ID)
                    .status(request.getStatus())
                    .build();
            MilitaryObligationResponseDto expected =
                    MilitaryObligationResponseDto.builder().id(OBLIGATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(militaryObligationRepository.existsByEmployeeId(EMPLOYEE_ID)).thenReturn(false);
            when(militaryObligationMapper.toEntity(request)).thenReturn(obligation);
            when(militaryObligationRepository.save(obligation)).thenReturn(obligation);
            when(militaryObligationMapper.toResponseDto(obligation)).thenReturn(expected);

            assertThat(militaryObligationService.create(EMPLOYEE_ID, request)).isSameAs(expected);
            assertThat(obligation.getEmployee()).isSameAs(employee);
            assertThat(employee.getMilitaryObligation()).isSameAs(obligation);
        }
    }

    @Nested
    @DisplayName("getByEmployeeId")
    class GetByEmployeeId {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.getByEmployeeId(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the employee has no military obligation")
        void shouldRejectMissingObligation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.getByEmployeeId(EMPLOYEE_ID))
                    .isInstanceOf(MilitaryObligationNotFoundException.class);
        }

        @Test
        @DisplayName("returns the military obligation of the employee")
        void shouldReturnObligation() {
            MilitaryObligation obligation = obligation();
            MilitaryObligationResponseDto expected =
                    MilitaryObligationResponseDto.builder().id(OBLIGATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(Optional.of(obligation));
            when(militaryObligationMapper.toResponseDto(obligation)).thenReturn(expected);

            assertThat(militaryObligationService.getByEmployeeId(EMPLOYEE_ID)).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when the employee has no military obligation")
        void shouldRejectMissingObligation() {
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.update(EMPLOYEE_ID, updateRequest()))
                    .isInstanceOf(MilitaryObligationNotFoundException.class);

            verifyNoInteractions(employeeRepository);
            verify(militaryObligationRepository, never()).save(any());
        }

        @Test
        @DisplayName("updates the military obligation")
        void shouldUpdateObligation() {
            MilitaryObligation obligation = obligation();
            MilitaryObligationUpdateRequestDto request = updateRequest();
            MilitaryObligationResponseDto expected =
                    MilitaryObligationResponseDto.builder().id(OBLIGATION_ID).build();

            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(Optional.of(obligation));
            when(militaryObligationRepository.save(obligation)).thenReturn(obligation);
            when(militaryObligationMapper.toResponseDto(obligation)).thenReturn(expected);

            assertThat(militaryObligationService.update(EMPLOYEE_ID, request)).isSameAs(expected);

            ArgumentCaptor<MilitaryObligationUpdateRequestDto> captor =
                    ArgumentCaptor.forClass(MilitaryObligationUpdateRequestDto.class);
            verify(militaryObligationMapper).updateEntity(captor.capture(), any(MilitaryObligation.class));
            assertThat(captor.getValue().getMilitaryRank()).isEqualTo("Captain");
        }
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.upsert(EMPLOYEE_ID, createRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("updates the existing obligation")
        void shouldUpdateExistingObligation() {
            MilitaryObligation existing = obligation();
            MilitaryObligationCreateRequestDto request = createRequest();
            MilitaryObligationResponseDto expected =
                    MilitaryObligationResponseDto.builder().id(OBLIGATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(Optional.of(existing));
            when(militaryObligationRepository.save(existing)).thenReturn(existing);
            when(militaryObligationMapper.toResponseDto(existing)).thenReturn(expected);

            assertThat(militaryObligationService.upsert(EMPLOYEE_ID, request)).isSameAs(expected);

            ArgumentCaptor<MilitaryObligationUpdateRequestDto> captor =
                    ArgumentCaptor.forClass(MilitaryObligationUpdateRequestDto.class);
            verify(militaryObligationMapper).updateEntity(captor.capture(), any(MilitaryObligation.class));

            MilitaryObligationUpdateRequestDto update = captor.getValue();
            assertThat(update.getStatus()).isEqualTo(request.getStatus());
            assertThat(update.getMilitaryRank()).isEqualTo(request.getMilitaryRank());
            assertThat(update.getCategory()).isEqualTo(request.getCategory());
            assertThat(update.getDocumentNumber()).isEqualTo(request.getDocumentNumber());

            verify(militaryObligationRepository, never()).existsByEmployeeId(any());
        }

        @Test
        @DisplayName("creates a new obligation when none exists")
        void shouldCreateWhenMissing() {
            MilitaryObligationCreateRequestDto request = createRequest();
            MilitaryObligation created = MilitaryObligation.builder().id(OBLIGATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());
            when(militaryObligationRepository.existsByEmployeeId(EMPLOYEE_ID)).thenReturn(false);
            when(militaryObligationMapper.toEntity(request)).thenReturn(created);
            when(militaryObligationRepository.save(created)).thenReturn(created);
            when(militaryObligationMapper.toResponseDto(created))
                    .thenReturn(MilitaryObligationResponseDto.builder().id(OBLIGATION_ID).build());

            assertThat(militaryObligationService.upsert(EMPLOYEE_ID, request)).isNotNull();

            verify(militaryObligationRepository).save(created);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.delete(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the employee has no military obligation")
        void shouldRejectMissingObligation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> militaryObligationService.delete(EMPLOYEE_ID))
                    .isInstanceOf(MilitaryObligationNotFoundException.class);

            verify(militaryObligationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes the obligation and detaches it from the employee")
        void shouldDeleteObligation() {
            Employee employee = employee();
            MilitaryObligation obligation = obligation();
            employee.setMilitaryObligation(obligation);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(militaryObligationRepository.findByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(Optional.of(obligation));

            assertThatCode(() -> militaryObligationService.delete(EMPLOYEE_ID))
                    .doesNotThrowAnyException();

            assertThat(employee.getMilitaryObligation()).isNull();
            verify(militaryObligationRepository).delete(obligation);
        }
    }

    private static MilitaryObligationCreateRequestDto createRequest() {
        return MilitaryObligationCreateRequestDto.builder()
                .status(MilitaryStatus.COMPLETED)
                .militaryRank("Captain")
                .category("A")
                .documentNumber("DOC-123")
                .build();
    }

    private static MilitaryObligationUpdateRequestDto updateRequest() {
        return MilitaryObligationUpdateRequestDto.builder()
                .status(MilitaryStatus.EXEMPT)
                .militaryRank("Captain")
                .category("B")
                .documentNumber("DOC-456")
                .build();
    }

    private static Employee employee() {
        return Employee.builder()
                .id(EMPLOYEE_ID)
                .firstName("Ivan")
                .lastName("Petrov")
                .email("ivan.petrov@company.com")
                .hireDate(LocalDate.of(2024, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    private static MilitaryObligation obligation() {
        return MilitaryObligation.builder()
                .id(OBLIGATION_ID)
                .employee(employee())
                .status(MilitaryStatus.COMPLETED)
                .militaryRank("Captain")
                .category("A")
                .documentNumber("DOC-123")
                .build();
    }
}
