package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.EducationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EducationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EducationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Education;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.enums.EducationLevel;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.common.InvalidDateRangeException;
import com.zodiakk.employeeservice.employee.exception.common.ResourceDoesNotBelongToEmployeeException;
import com.zodiakk.employeeservice.employee.exception.education.EducationNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EducationMapper;
import com.zodiakk.employeeservice.employee.repository.EducationRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
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
class EducationServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID EDUCATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID OTHER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final LocalDate START_DATE = LocalDate.of(2015, 9, 1);

    private static final LocalDate END_DATE = LocalDate.of(2020, 6, 30);

    @Mock
    private EducationRepository educationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EducationMapper educationMapper;

    @InjectMocks
    private EducationServiceImpl educationService;

    @Nested
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.add(EMPLOYEE_ID, createRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(educationRepository, educationMapper);
        }

        @Test
        @DisplayName("throws when the end date is before the start date")
        void shouldRejectInvalidDateRange() {
            EducationCreateRequestDto request = createRequest();
            request.setEndDate(START_DATE.minusDays(1));

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));

            assertThatThrownBy(() -> educationService.add(EMPLOYEE_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class);

            verifyNoInteractions(educationRepository, educationMapper);
        }

        @Test
        @DisplayName("adds an education entry bound to the employee")
        void shouldAddEducation() {
            Employee employee = employee(EMPLOYEE_ID);
            EducationCreateRequestDto request = createRequest();
            Education education = Education.builder().id(EDUCATION_ID).build();
            EducationResponseDto expected = EducationResponseDto.builder().id(EDUCATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(educationMapper.toEntity(request)).thenReturn(education);
            when(educationRepository.save(education)).thenReturn(education);
            when(educationMapper.toResponseDto(education)).thenReturn(expected);

            assertThat(educationService.add(EMPLOYEE_ID, request)).isSameAs(expected);
            assertThat(education.getEmployee()).isSameAs(employee);
        }

        @Test
        @DisplayName("accepts an education entry without dates")
        void shouldAllowMissingDates() {
            EducationCreateRequestDto request = createRequest();
            request.setStartDate(null);
            request.setEndDate(null);
            Education education = Education.builder().id(EDUCATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationMapper.toEntity(request)).thenReturn(education);
            when(educationRepository.save(education)).thenReturn(education);
            when(educationMapper.toResponseDto(education))
                    .thenReturn(EducationResponseDto.builder().build());

            assertThatCode(() -> educationService.add(EMPLOYEE_ID, request)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.getById(EMPLOYEE_ID, EDUCATION_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the education does not exist")
        void shouldRejectMissingEducation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.getById(EMPLOYEE_ID, EDUCATION_ID))
                    .isInstanceOf(EducationNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the education belongs to another employee")
        void shouldRejectForeignEducation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID))
                    .thenReturn(Optional.of(education(employee(OTHER_ID))));

            assertThatThrownBy(() -> educationService.getById(EMPLOYEE_ID, EDUCATION_ID))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class);
        }

        @Test
        @DisplayName("returns the education of the employee")
        void shouldReturnEducation() {
            Education education = education(employee(EMPLOYEE_ID));
            EducationResponseDto expected = EducationResponseDto.builder().id(EDUCATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.of(education));
            when(educationMapper.toResponseDto(education)).thenReturn(expected);

            assertThat(educationService.getById(EMPLOYEE_ID, EDUCATION_ID)).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("listByEmployee")
    class ListByEmployee {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.listByEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("maps every education entry of the employee")
        void shouldReturnEducations() {
            Education first = Education.builder().id(EDUCATION_ID).build();
            Education second = Education.builder().id(OTHER_ID).build();
            EducationResponseDto firstDto = EducationResponseDto.builder().id(EDUCATION_ID).build();
            EducationResponseDto secondDto = EducationResponseDto.builder().id(OTHER_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findAllByEmployeeIdOrderByStartDateDesc(EMPLOYEE_ID))
                    .thenReturn(List.of(first, second));
            when(educationMapper.toResponseDto(first)).thenReturn(firstDto);
            when(educationMapper.toResponseDto(second)).thenReturn(secondDto);

            assertThat(educationService.listByEmployee(EMPLOYEE_ID)).containsExactly(firstDto, secondDto);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.update(EMPLOYEE_ID, EDUCATION_ID, updateRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the education does not exist")
        void shouldRejectMissingEducation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.update(EMPLOYEE_ID, EDUCATION_ID, updateRequest()))
                    .isInstanceOf(EducationNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the education belongs to another employee")
        void shouldRejectForeignEducation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID))
                    .thenReturn(Optional.of(education(employee(OTHER_ID))));

            assertThatThrownBy(() -> educationService.update(EMPLOYEE_ID, EDUCATION_ID, updateRequest()))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class);
        }

        @Test
        @DisplayName("throws when the requested date range is invalid")
        void shouldRejectInvalidRequestedDates() {
            EducationUpdateRequestDto request = EducationUpdateRequestDto.builder()
                    .startDate(END_DATE)
                    .endDate(START_DATE)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID))
                    .thenReturn(Optional.of(education(employee(EMPLOYEE_ID))));

            assertThatThrownBy(() -> educationService.update(EMPLOYEE_ID, EDUCATION_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class);

            verify(educationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the stored date range becomes invalid")
        void shouldRejectInvalidStoredDates() {
            Education education = education(employee(EMPLOYEE_ID));
            education.setStartDate(END_DATE);
            education.setEndDate(START_DATE);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.of(education));

            assertThatThrownBy(() -> educationService.update(EMPLOYEE_ID, EDUCATION_ID, updateRequest()))
                    .isInstanceOf(InvalidDateRangeException.class);
        }

        @Test
        @DisplayName("updates the education entry")
        void shouldUpdateEducation() {
            Education education = education(employee(EMPLOYEE_ID));
            EducationUpdateRequestDto request = EducationUpdateRequestDto.builder()
                    .institutionName("BSU")
                    .startDate(START_DATE.plusYears(1))
                    .build();
            EducationResponseDto expected = EducationResponseDto.builder().id(EDUCATION_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.of(education));
            when(educationRepository.save(education)).thenReturn(education);
            when(educationMapper.toResponseDto(education)).thenReturn(expected);

            assertThat(educationService.update(EMPLOYEE_ID, EDUCATION_ID, request)).isSameAs(expected);

            ArgumentCaptor<EducationUpdateRequestDto> captor =
                    ArgumentCaptor.forClass(EducationUpdateRequestDto.class);
            verify(educationMapper).updateEntity(captor.capture(), any(Education.class));
            assertThat(captor.getValue().getInstitutionName()).isEqualTo("BSU");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.delete(EMPLOYEE_ID, EDUCATION_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the education does not exist")
        void shouldRejectMissingEducation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> educationService.delete(EMPLOYEE_ID, EDUCATION_ID))
                    .isInstanceOf(EducationNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the education belongs to another employee")
        void shouldRejectForeignEducation() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID))
                    .thenReturn(Optional.of(education(employee(OTHER_ID))));

            assertThatThrownBy(() -> educationService.delete(EMPLOYEE_ID, EDUCATION_ID))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class);

            verify(educationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deletes the education entry")
        void shouldDeleteEducation() {
            Education education = education(employee(EMPLOYEE_ID));

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(educationRepository.findById(EDUCATION_ID)).thenReturn(Optional.of(education));

            assertThatCode(() -> educationService.delete(EMPLOYEE_ID, EDUCATION_ID))
                    .doesNotThrowAnyException();

            verify(educationRepository).delete(education);
        }
    }

    private static EducationCreateRequestDto createRequest() {
        return EducationCreateRequestDto.builder()
                .educationLevel(EducationLevel.BACHELOR)
                .institutionName("BSUIR")
                .fieldOfStudy("Computer Science")
                .degreeName("Software Engineer")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }

    private static EducationUpdateRequestDto updateRequest() {
        return EducationUpdateRequestDto.builder().build();
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

    private static Education education(Employee employee) {
        return Education.builder()
                .id(EDUCATION_ID)
                .employee(employee)
                .educationLevel(EducationLevel.BACHELOR)
                .institutionName("BSUIR")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();
    }
}
