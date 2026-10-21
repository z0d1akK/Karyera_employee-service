package com.zodiakk.employeeservice.employee;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import com.zodiakk.employeeservice.employee.entity.Grade;
import com.zodiakk.employeeservice.employee.entity.Position;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Employee controller")
class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/employees";

    @Nested
    @DisplayName("POST /api/employees")
    class Create {

        @Test
        @DisplayName("creates an employee with resolved catalogs and a normalized email")
        void shouldCreateEmployee() throws Exception {
            Position position = persistPosition("Software Engineer");
            Grade grade = persistGrade("Senior", 3);
            Department department = persistDepartment("Backend");
            Specialization specialization = persistSpecialization("Java");
            UUID userId = UUID.randomUUID();

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(createRequest(
                                    userId, position, grade, department, specialization
                            ))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.firstName").value("Ivan"))
                    .andExpect(jsonPath("$.lastName").value("Petrov"))
                    .andExpect(jsonPath("$.email").value("ivan.petrov@company.com"))
                    .andExpect(jsonPath("$.employmentStatus").value("ACTIVE"))
                    .andExpect(jsonPath("$.position.id").value(position.getId().toString()))
                    .andExpect(jsonPath("$.grade.id").value(grade.getId().toString()))
                    .andExpect(jsonPath("$.department.id").value(department.getId().toString()))
                    .andExpect(jsonPath("$.specialization.id").value(specialization.getId().toString()));

            Employee stored = employeeRepository.findByUserId(userId).orElseThrow();
            assertThat(stored.getEmail()).isEqualTo("ivan.petrov@company.com");
            assertThat(stored.getPosition().getId()).isEqualTo(position.getId());
        }

        @Test
        @DisplayName("creates an employee without optional catalog references")
        void shouldCreateEmployeeWithoutOptionalCatalogs() throws Exception {
            Position position = persistPosition("Analyst");
            Grade grade = persistGrade("Junior", 1);

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(createRequest(
                                    UUID.randomUUID(), position, grade, null, null
                            ))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.department").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.specialization").value(org.hamcrest.Matchers.nullValue()));
        }

        @Test
        @DisplayName("returns validation errors for an empty request")
        void shouldReturnValidationErrors() throws Exception {
            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }

        @Test
        @DisplayName("returns validation errors for an invalid email")
        void shouldRejectInvalidEmail() throws Exception {
            Position position = persistPosition("Tester");
            Grade grade = persistGrade("Middle", 2);
            EmployeeCreateRequestDto request = createRequest(
                    UUID.randomUUID(), position, grade, null, null
            );
            request.setEmail("not-an-email");

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("email"));
        }

        @Test
        @DisplayName("rejects a duplicate user id")
        void shouldRejectDuplicateUserId() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            UUID userId = UUID.randomUUID();
            persistEmployee(userId, position, grade, EmploymentStatus.ACTIVE);

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(createRequest(
                                    userId, position, grade, null, null
                            ))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                            userId.toString()
                    )));
        }

        @Test
        @DisplayName("rejects a duplicate email regardless of case")
        void shouldRejectDuplicateEmail() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            persistEmployee(
                    UUID.randomUUID(), "ivan.petrov@company.com", position, grade,
                    EmploymentStatus.ACTIVE, LocalDate.now().minusYears(1)
            );

            EmployeeCreateRequestDto request = createRequest(
                    UUID.randomUUID(), position, grade, null, null
            );
            request.setEmail("IVAN.PETROV@COMPANY.COM");

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects creating an employee in the terminated status")
        void shouldRejectTerminatedEmployeeCreation() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            EmployeeCreateRequestDto request = createRequest(
                    UUID.randomUUID(), position, grade, null, null
            );
            request.setEmploymentStatus(EmploymentStatus.TERMINATED);

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects an inactive position")
        void shouldRejectInactivePosition() throws Exception {
            Position position = persistPosition("Retired", false);
            Grade grade = persistGrade("Lead", 4);

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(createRequest(
                                    UUID.randomUUID(), position, grade, null, null
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 for an unknown grade")
        void shouldReturnNotFoundForUnknownGrade() throws Exception {
            Position position = persistPosition("Developer");
            EmployeeCreateRequestDto request = createRequest(
                    UUID.randomUUID(), position, null, null, null
            );
            request.setGradeId(UUID.randomUUID());

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("rejects a hire date in the future")
        void shouldRejectFutureHireDate() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            EmployeeCreateRequestDto request = createRequest(
                    UUID.randomUUID(), position, grade, null, null
            );
            request.setHireDate(LocalDate.now().plusDays(1));

            mockMvc.perform(asHr(post(BASE_URL))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("forbids a non-privileged user to create employees")
        void shouldForbidEmployeeCreation() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);

            mockMvc.perform(asEmployee(post(BASE_URL), UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(createRequest(
                                    UUID.randomUUID(), position, grade, null, null
                            ))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/employees")
    class Read {

        @Test
        @DisplayName("returns an employee by id for an HR user")
        void shouldReturnEmployeeById() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asHr(get(BASE_URL + "/" + employee.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employee.getId().toString()))
                    .andExpect(jsonPath("$.email").value(employee.getEmail()))
                    .andExpect(jsonPath("$.responsibilities").isArray());
        }

        @Test
        @DisplayName("returns an employee by id for the employee himself")
        void shouldReturnEmployeeForSelf() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asEmployee(get(BASE_URL + "/" + employee.getId()), employee.getUserId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employee.getId().toString()));
        }

        @Test
        @DisplayName("forbids another employee to read the profile")
        void shouldForbidForeignProfileRead() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asEmployee(get(BASE_URL + "/" + employee.getId()), UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("returns 404 for an unknown employee")
        void shouldReturnNotFoundById() throws Exception {
            mockMvc.perform(asHr(get(BASE_URL + "/" + UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns an employee by user id")
        void shouldReturnEmployeeByUserId() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asHr(get(BASE_URL + "/user/" + employee.getUserId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employee.getId().toString()));
        }

        @Test
        @DisplayName("returns the own profile by user id")
        void shouldReturnOwnProfileByUserId() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asEmployee(get(BASE_URL + "/user/" + employee.getUserId()), employee.getUserId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("forbids reading a foreign profile by user id")
        void shouldForbidForeignUserRead() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asEmployee(get(BASE_URL + "/user/" + employee.getUserId()), UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("returns 404 for an unknown user id")
        void shouldReturnNotFoundByUserId() throws Exception {
            mockMvc.perform(asHr(get(BASE_URL + "/user/" + UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns a filtered and paginated list")
        void shouldReturnFilteredPage() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            persistEmployee(position, grade);
            Employee target = persistEmployee(position, grade);
            target.setFirstName("Petr");
            employeeRepository.saveAndFlush(target);

            mockMvc.perform(asHr(get(BASE_URL)
                            .param("firstName", "petr")
                            .param("employmentStatus", "ACTIVE")
                            .param("page", "0")
                            .param("size", "10")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(target.getId().toString()))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("allows a manager to list employees")
        void shouldAllowManagerToList() throws Exception {
            persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asManager(get(BASE_URL), UUID.randomUUID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("forbids a regular employee to list employees")
        void shouldForbidEmployeeToList() throws Exception {
            mockMvc.perform(asEmployee(get(BASE_URL), UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("returns 400 for invalid paging parameters")
        void shouldRejectInvalidPaging() throws Exception {
            mockMvc.perform(asHr(get(BASE_URL).param("size", "0")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 for an invalid status filter")
        void shouldRejectInvalidStatusFilter() throws Exception {
            mockMvc.perform(asHr(get(BASE_URL).param("employmentStatus", "UNKNOWN")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/employees/{id}")
    class Update {

        @Test
        @DisplayName("lets HR update all profile fields")
        void shouldUpdateEmployeeAsHr() throws Exception {
            Position originalPosition = persistPosition("Developer");
            Position newPosition = persistPosition("Architect");
            Grade grade = persistGrade("Lead", 4);
            Department department = persistDepartment("Platform");
            Employee employee = persistEmployee(originalPosition, grade);

            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder()
                    .firstName("Sergey")
                    .lastName("Sidorov")
                    .middleName("Petrovich")
                    .email("Sergey.Sidorov@Company.com")
                    .birthDate(LocalDate.of(1988, 5, 5))
                    .hireDate(LocalDate.now().minusYears(2))
                    .positionId(newPosition.getId())
                    .departmentId(department.getId())
                    .maxSubordinates(10)
                    .employmentStatus(EmploymentStatus.ON_LEAVE)
                    .build();

            mockMvc.perform(asHr(put(BASE_URL + "/" + employee.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Sergey"))
                    .andExpect(jsonPath("$.email").value("sergey.sidorov@company.com"))
                    .andExpect(jsonPath("$.position.id").value(newPosition.getId().toString()))
                    .andExpect(jsonPath("$.department.id").value(department.getId().toString()))
                    .andExpect(jsonPath("$.maxSubordinates").value(10))
                    .andExpect(jsonPath("$.employmentStatus").value("ON_LEAVE"));
        }

        @Test
        @DisplayName("lets an employee update his own names")
        void shouldLetEmployeeUpdateOwnNames() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder()
                    .firstName("Ivan")
                    .lastName("Novikov")
                    .email(employee.getEmail())
                    .build();

            mockMvc.perform(asEmployee(put(BASE_URL + "/" + employee.getId()), employee.getUserId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lastName").value("Novikov"));
        }

        @Test
        @DisplayName("forbids an employee to change his position")
        void shouldForbidEmployeePositionChange() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));
            Position newPosition = persistPosition("Architect");

            EmployeeUpdateRequestDto request = EmployeeUpdateRequestDto.builder()
                    .email(employee.getEmail())
                    .positionId(newPosition.getId())
                    .build();

            mockMvc.perform(asEmployee(put(BASE_URL + "/" + employee.getId()), employee.getUserId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("forbids another employee to update the profile")
        void shouldForbidForeignUpdate() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asEmployee(put(BASE_URL + "/" + employee.getId()), UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeUpdateRequestDto.builder()
                                    .email(employee.getEmail())
                                    .firstName("Hacker")
                                    .build())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("returns 404 for an unknown employee")
        void shouldReturnNotFoundOnUpdate() throws Exception {
            mockMvc.perform(asHr(put(BASE_URL + "/" + UUID.randomUUID()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeUpdateRequestDto.builder()
                                    .email("someone@company.com")
                                    .build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("rejects an email already used by another employee")
        void shouldRejectDuplicateEmailOnUpdate() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            Employee first = persistEmployee(position, grade);
            Employee second = persistEmployee(position, grade);

            mockMvc.perform(asHr(put(BASE_URL + "/" + second.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeUpdateRequestDto.builder()
                                    .email(first.getEmail())
                                    .build())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects an inconsistent birth date")
        void shouldRejectInvalidDates() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asHr(put(BASE_URL + "/" + employee.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeUpdateRequestDto.builder()
                                    .email(employee.getEmail())
                                    .birthDate(LocalDate.now().minusDays(1))
                                    .hireDate(LocalDate.now().minusYears(1))
                                    .build())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/employees/{id}/status")
    class ChangeStatus {

        @Test
        @DisplayName("changes the employment status")
        void shouldChangeStatus() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asHr(patch(BASE_URL + "/" + employee.getId() + "/status")
                            .param("status", "ON_LEAVE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employmentStatus").value("ON_LEAVE"));
        }

        @Test
        @DisplayName("closes active responsibilities when the employee is terminated")
        void shouldCloseResponsibilitiesOnTermination() throws Exception {
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            Employee employee = persistEmployee(position, grade);
            Employee manager = persistEmployee(position, grade);
            ResponsibilityType type = persistResponsibilityType("TEAM_LEAD", true, true);

            EmployeeResponsibility responsibility = employeeResponsibilityRepository.saveAndFlush(
                    EmployeeResponsibility.builder()
                            .employee(employee)
                            .responsibleEmployee(manager)
                            .responsibilityType(type)
                            .startDate(LocalDate.now().minusMonths(1))
                            .build()
            );

            mockMvc.perform(asHr(patch(BASE_URL + "/" + employee.getId() + "/status")
                            .param("status", "TERMINATED")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employmentStatus").value("TERMINATED"));

            EmployeeResponsibility stored = employeeResponsibilityRepository
                    .findById(responsibility.getId())
                    .orElseThrow();
            assertThat(stored.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("returns 400 for an unknown status")
        void shouldRejectUnknownStatus() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asHr(patch(BASE_URL + "/" + employee.getId() + "/status")
                            .param("status", "UNKNOWN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 for an unknown employee")
        void shouldReturnNotFoundOnStatusChange() throws Exception {
            mockMvc.perform(asHr(patch(BASE_URL + "/" + UUID.randomUUID() + "/status")
                            .param("status", "ACTIVE")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("forbids a manager to change the status")
        void shouldForbidManagerStatusChange() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asManager(patch(BASE_URL + "/" + employee.getId() + "/status")
                            .param("status", "ACTIVE"), UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/employees/{id}")
    class Delete {

        @Test
        @DisplayName("deletes a terminated employee")
        void shouldDeleteTerminatedEmployee() throws Exception {
            Employee employee = persistEmployee(
                    UUID.randomUUID(),
                    persistPosition("Developer"),
                    persistGrade("Lead", 4),
                    EmploymentStatus.TERMINATED
            );

            mockMvc.perform(asHr(delete(BASE_URL + "/" + employee.getId())))
                    .andExpect(status().isNoContent());

            assertThat(employeeRepository.findById(employee.getId())).isEmpty();
        }

        @Test
        @DisplayName("rejects deleting an employee that is not terminated")
        void shouldRejectActiveEmployeeDeletion() throws Exception {
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

            mockMvc.perform(asHr(delete(BASE_URL + "/" + employee.getId())))
                    .andExpect(status().isBadRequest());

            assertThat(employeeRepository.findById(employee.getId())).isPresent();
        }

        @Test
        @DisplayName("returns 404 for an unknown employee")
        void shouldReturnNotFoundOnDelete() throws Exception {
            mockMvc.perform(asHr(delete(BASE_URL + "/" + UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }
    }

    private EmployeeCreateRequestDto createRequest(
            UUID userId,
            Position position,
            Grade grade,
            Department department,
            Specialization specialization
    ) {
        return EmployeeCreateRequestDto.builder()
                .userId(userId)
                .firstName("Ivan")
                .lastName("Petrov")
                .middleName("Ivanovich")
                .email("Ivan.Petrov@Company.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .hireDate(LocalDate.now().minusMonths(1))
                .positionId(position != null ? position.getId() : null)
                .gradeId(grade != null ? grade.getId() : null)
                .departmentId(department != null ? department.getId() : null)
                .specializationId(specialization != null ? specialization.getId() : null)
                .maxSubordinates(5)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }
}
