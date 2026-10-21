package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.ExperienceCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ExperienceUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ExperienceResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.Experience;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.common.InvalidDateRangeException;
import com.zodiakk.employeeservice.employee.exception.common.ResourceDoesNotBelongToEmployeeException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.experience.ExperienceNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.ExperienceMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.ExperienceRepository;
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
class ExperienceServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID EXPERIENCE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID OTHER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final LocalDate START_DATE = LocalDate.of(2018, 3, 1);

    private static final LocalDate END_DATE = LocalDate.of(2023, 12, 31);

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ExperienceMapper experienceMapper;

    @InjectMocks
    private ExperienceServiceImpl experienceService;

    @Nested
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.add(EMPLOYEE_ID, createRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(experienceRepository, experienceMapper);
        }

        @Test
        @DisplayName("throws when the end date is before the start date")
        void shouldRejectInvalidDateRange() {
            ExperienceCreateRequestDto request = createRequest();
            request.setEndDate(START_DATE.minusDays(1));

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));

            assertThatThrownBy(() -> experienceService.add(EMPLOYEE_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class);

            verifyNoInteractions(experienceRepository, experienceMapper);
        }

        @Test
        @DisplayName("adds an experience entry bound to the employee")
        void shouldAddExperience() {
            Employee employee = employee(EMPLOYEE_ID);
            ExperienceCreateRequestDto request = createRequest();
            Experience experience = Experience.builder().id(EXPERIENCE_ID).build();
            ExperienceResponseDto expected = ExperienceResponseDto.builder().id(EXPERIENCE_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(experienceMapper.toEntity(request)).thenReturn(experience);
            when(experienceRepository.save(experience)).thenReturn(experience);
            when(experienceMapper.toResponseDto(experience)).thenReturn(expected);

            assertThat(experienceService.add(EMPLOYEE_ID, request)).isSameAs(expected);
            assertThat(experience.getEmployee()).isSameAs(employee);

            ArgumentCaptor<Experience> captor = ArgumentCaptor.forClass(Experience.class);
            verify(experienceRepository).save(captor.capture());
            assertThat(captor.getValue().getEmployee()).isSameAs(employee);
        }

        @Test
        @DisplayName("accepts an experience entry without dates")
        void shouldAllowMissingDates() {
            ExperienceCreateRequestDto request = createRequest();
            request.setStartDate(null);
            request.setEndDate(null);
            Experience experience = Experience.builder().id(EXPERIENCE_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceMapper.toEntity(request)).thenReturn(experience);
            when(experienceRepository.save(experience)).thenReturn(experience);
            when(experienceMapper.toResponseDto(experience))
                    .thenReturn(ExperienceResponseDto.builder().build());

            assertThatCode(() -> experienceService.add(EMPLOYEE_ID, request)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.getById(EMPLOYEE_ID, EXPERIENCE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the experience does not exist")
        void shouldRejectMissingExperience() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.getById(EMPLOYEE_ID, EXPERIENCE_ID))
                    .isInstanceOf(ExperienceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the experience belongs to another employee")
        void shouldRejectForeignExperience() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID))
                    .thenReturn(Optional.of(experience(employee(OTHER_ID))));

            assertThatThrownBy(() -> experienceService.getById(EMPLOYEE_ID, EXPERIENCE_ID))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class);
        }

        @Test
        @DisplayName("returns the experience of the employee")
        void shouldReturnExperience() {
            Experience experience = experience(employee(EMPLOYEE_ID));
            ExperienceResponseDto expected = ExperienceResponseDto.builder().id(EXPERIENCE_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));
            when(experienceMapper.toResponseDto(experience)).thenReturn(expected);

            assertThat(experienceService.getById(EMPLOYEE_ID, EXPERIENCE_ID)).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("listByEmployee")
    class ListByEmployee {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.listByEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("maps every experience entry of the employee")
        void shouldReturnExperiences() {
            Experience first = Experience.builder().id(EXPERIENCE_ID).build();
            Experience second = Experience.builder().id(OTHER_ID).build();
            ExperienceResponseDto firstDto = ExperienceResponseDto.builder().id(EXPERIENCE_ID).build();
            ExperienceResponseDto secondDto = ExperienceResponseDto.builder().id(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findAllByEmployeeIdOrderByStartDateDesc(EMPLOYEE_ID))
                    .thenReturn(List.of(first, second));
            when(experienceMapper.toResponseDto(first)).thenReturn(firstDto);
            when(experienceMapper.toResponseDto(second)).thenReturn(secondDto);

            assertThat(experienceService.listByEmployee(EMPLOYEE_ID)).containsExactly(firstDto, secondDto);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.update(EMPLOYEE_ID, EXPERIENCE_ID, updateRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the experience does not exist")
        void shouldRejectMissingExperience() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.update(EMPLOYEE_ID, EXPERIENCE_ID, updateRequest()))
                    .isInstanceOf(ExperienceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the experience belongs to another employee")
        void shouldRejectForeignExperience() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID))
                    .thenReturn(Optional.of(experience(employee(OTHER_ID))));

            assertThatThrownBy(() -> experienceService.update(EMPLOYEE_ID, EXPERIENCE_ID, updateRequest()))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class);
        }

        @Test
        @DisplayName("throws when the requested date range is invalid")
        void shouldRejectInvalidRequestedDates() {
            ExperienceUpdateRequestDto request = ExperienceUpdateRequestDto.builder()
                    .startDate(END_DATE)
                    .endDate(START_DATE)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID))
                    .thenReturn(Optional.of(experience(employee(EMPLOYEE_ID))));

            assertThatThrownBy(() -> experienceService.update(EMPLOYEE_ID, EXPERIENCE_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class);

            verify(experienceRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the stored date range becomes invalid")
        void shouldRejectInvalidStoredDates() {
            Experience experience = experience(employee(EMPLOYEE_ID));
            experience.setStartDate(END_DATE);
            experience.setEndDate(START_DATE);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));

            assertThatThrownBy(() -> experienceService.update(EMPLOYEE_ID, EXPERIENCE_ID, updateRequest()))
                    .isInstanceOf(InvalidDateRangeException.class);
        }

        @Test
        @DisplayName("updates the experience entry")
        void shouldUpdateExperience() {
            Experience experience = experience(employee(EMPLOYEE_ID));
            ExperienceUpdateRequestDto request = ExperienceUpdateRequestDto.builder()
                    .company("New Company")
                    .endDate(END_DATE.plusMonths(1))
                    .build();
            ExperienceResponseDto expected = ExperienceResponseDto.builder().id(EXPERIENCE_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));
            when(experienceRepository.save(experience)).thenReturn(experience);
            when(experienceMapper.toResponseDto(experience)).thenReturn(expected);

            assertThat(experienceService.update(EMPLOYEE_ID, EXPERIENCE_ID, request)).isSameAs(expected);

            ArgumentCaptor<ExperienceUpdateRequestDto> captor =
                    ArgumentCaptor.forClass(ExperienceUpdateRequestDto.class);
            verify(experienceMapper).updateEntity(captor.capture(), any(Experience.class));
            assertThat(captor.getValue().getCompany()).isEqualTo("New Company");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.delete(EMPLOYEE_ID, EXPERIENCE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the experience does not exist")
        void shouldRejectMissingExperience() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.delete(EMPLOYEE_ID, EXPERIENCE_ID))
                    .isInstanceOf(ExperienceNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the experience belongs to another employee")
        void shouldRejectForeignExperience() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID))
                    .thenReturn(Optional.of(experience(employee(OTHER_ID))));

            assertThatThrownBy(() -> experienceService.delete(EMPLOYEE_ID, EXPERIENCE_ID))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class);

            verify(experienceRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes the experience entry")
        void shouldDeleteExperience() {
            Experience experience = experience(employee(EMPLOYEE_ID));

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));

            assertThatCode(() -> experienceService.delete(EMPLOYEE_ID, EXPERIENCE_ID))
                    .doesNotThrowAnyException();

            verify(experienceRepository).delete(experience);
        }
    }

    private static ExperienceCreateRequestDto createRequest() {
        return ExperienceCreateRequestDto.builder()
                .company("Acme")
                .position("Software Engineer")
                .description("Backend development")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }

    private static ExperienceUpdateRequestDto updateRequest() {
        return ExperienceUpdateRequestDto.builder().build();
    }

    private static Employee employee(UUID id) {
        return Employee.builder()
                .id(id)
                .firstName("Ivan")
                .lastName("Petrov")
                .email(id + "@company.com")
                .hireDate(LocalDate.of(2024, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    private static Experience experience(Employee employee) {
        return Experience.builder()
                .id(EXPERIENCE_ID)
                .employee(employee)
                .company("Acme")
                .position("Software Engineer")
                .description("Backend development")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }
}
