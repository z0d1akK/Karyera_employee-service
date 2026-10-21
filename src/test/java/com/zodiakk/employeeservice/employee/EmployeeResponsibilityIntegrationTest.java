package com.zodiakk.employeeservice.employee;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeResponsibilityCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeResponsibilityUpdateRequestDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

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

@DisplayName("Employee responsibilities")
class EmployeeResponsibilityIntegrationTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("assign")
    class Assign {

        @Test
        @DisplayName("assigns a responsibility and lists it from both sides")
        void shouldAssignResponsibility() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            UUID responsibilityId = idOf(mockMvc.perform(asHr(post(base(employee)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request(manager, type, LocalDate.now().minusDays(10), null))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.employeeId").value(employee.getId().toString()))
                    .andExpect(jsonPath("$.responsibleEmployee.id").value(manager.getId().toString()))
                    .andExpect(jsonPath("$.responsibilityType.code").value("MENTOR")));

            mockMvc.perform(asHr(get(base(employee))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(responsibilityId.toString()));

            mockMvc.perform(asHr(get("/api/employees/responsibilities/responsible/" + manager.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("allows several active responsibilities when the type permits it")
        void shouldAllowMultipleAssignments() throws Exception {
            Employee first = newEmployee(EmploymentStatus.ACTIVE);
            Employee second = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("CURATOR", true, true);

            mockMvc.perform(asHr(post(base(first)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isCreated());

            mockMvc.perform(asHr(post(base(second)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("rejects self responsibility")
        void shouldRejectSelfResponsibility() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(employee, type, LocalDate.now(), null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects a terminated employee")
        void shouldRejectTerminatedEmployee() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.TERMINATED);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects an inactive responsibility type")
        void shouldRejectInactiveType() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, false);

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects an invalid date range")
        void shouldRejectInvalidDateRange() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(
                                    manager, type, LocalDate.now(), LocalDate.now().minusDays(1)
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects a duplicate active assignment")
        void shouldRejectDuplicateAssignment() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", false, true);
            EmployeeResponsibilityCreateRequestDto request =
                    request(manager, type, LocalDate.now(), null);

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects a second responsibility of a single-assignment type")
        void shouldRejectMultipleNotAllowed() throws Exception {
            Employee first = newEmployee(EmploymentStatus.ACTIVE);
            Employee second = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", false, true);

            mockMvc.perform(asHr(post(base(first)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isCreated());

            mockMvc.perform(asHr(post(base(second)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects exceeding the manager's subordinate limit")
        void shouldRejectMaxSubordinates() throws Exception {
            Employee first = newEmployee(EmploymentStatus.ACTIVE);
            Employee second = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            manager.setMaxSubordinates(1);
            employeeRepository.saveAndFlush(manager);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            mockMvc.perform(asHr(post(base(first)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isCreated());

            mockMvc.perform(asHr(post(base(second)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 404 for unknown employees and types")
        void shouldReturnNotFound() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            mockMvc.perform(asHr(post("/api/employees/" + UUID.randomUUID() + "/responsibilities"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isNotFound());

            mockMvc.perform(asHr(post(base(employee)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeResponsibilityCreateRequestDto.builder()
                                    .responsibleEmployeeId(manager.getId())
                                    .responsibilityTypeId(UUID.randomUUID())
                                    .startDate(LocalDate.now())
                                    .build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("forbids a manager to assign responsibilities")
        void shouldForbidManagerAssignment() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            mockMvc.perform(asManager(post(base(employee)), UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("close, update and delete")
    class Manage {

        @Test
        @DisplayName("closes, updates and deletes a responsibility")
        void shouldCloseUpdateAndDelete() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            Employee newManager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);
            ResponsibilityType newType = persistResponsibilityType("SUPERVISOR", true, true);

            UUID responsibilityId = idOf(mockMvc.perform(asHr(post(base(employee)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request(manager, type, LocalDate.now().minusDays(10), null))))
                    .andExpect(status().isCreated()));

            mockMvc.perform(asHr(put("/api/employees/responsibilities/" + responsibilityId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeResponsibilityUpdateRequestDto.builder()
                                    .responsibleEmployeeId(newManager.getId())
                                    .responsibilityTypeId(newType.getId())
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.responsibleEmployee.id").value(newManager.getId().toString()))
                    .andExpect(jsonPath("$.responsibilityType.code").value("SUPERVISOR"));

            LocalDate endDate = LocalDate.now();
            mockMvc.perform(asHr(patch("/api/employees/responsibilities/" + responsibilityId + "/close")
                            .param("endDate", endDate.toString())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.endDate").value(endDate.toString()));

            mockMvc.perform(asHr(delete("/api/employees/responsibilities/" + responsibilityId)))
                    .andExpect(status().isNoContent());

            assertThat(employeeResponsibilityRepository.findById(responsibilityId)).isEmpty();
        }

        @Test
        @DisplayName("rejects an invalid close date")
        void shouldRejectInvalidCloseDate() throws Exception {
            Employee employee = newEmployee(EmploymentStatus.ACTIVE);
            Employee manager = newEmployee(EmploymentStatus.ACTIVE);
            ResponsibilityType type = persistResponsibilityType("MENTOR", true, true);

            UUID responsibilityId = idOf(mockMvc.perform(asHr(post(base(employee)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request(manager, type, LocalDate.now(), null))))
                    .andExpect(status().isCreated()));

            mockMvc.perform(asHr(patch("/api/employees/responsibilities/" + responsibilityId + "/close")
                            .param("endDate", LocalDate.now().minusDays(1).toString())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 for unknown responsibilities")
        void shouldReturnNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(asHr(delete("/api/employees/responsibilities/" + id)))
                    .andExpect(status().isNotFound());
            mockMvc.perform(asHr(put("/api/employees/responsibilities/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
            mockMvc.perform(asHr(patch("/api/employees/responsibilities/" + id + "/close")
                            .param("endDate", LocalDate.now().toString())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 404 when listing responsibilities of an unknown employee")
        void shouldReturnNotFoundForUnknownEmployee() throws Exception {
            mockMvc.perform(asHr(get("/api/employees/" + UUID.randomUUID() + "/responsibilities")))
                    .andExpect(status().isNotFound());
        }
    }

    private Employee newEmployee(EmploymentStatus status) {
        return persistEmployee(
                UUID.randomUUID(),
                persistPosition("Developer-" + UUID.randomUUID()),
                persistGrade("Lead-" + UUID.randomUUID(), nextGradeLevel()),
                status
        );
    }

    private String base(Employee employee) {
        return "/api/employees/" + employee.getId() + "/responsibilities";
    }

    private EmployeeResponsibilityCreateRequestDto request(
            Employee responsible,
            ResponsibilityType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return EmployeeResponsibilityCreateRequestDto.builder()
                .responsibleEmployeeId(responsible.getId())
                .responsibilityTypeId(type.getId())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private UUID idOf(ResultActions actions) throws Exception {
        String body = actions.andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }
}
