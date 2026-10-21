package com.zodiakk.employeeservice.employee;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.dto.request.create.DepartmentCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.GradeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.PositionCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.ResponsibilityTypeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.SkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.SpecializationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.GradeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.PositionUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ResponsibilityTypeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SpecializationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import com.zodiakk.employeeservice.employee.entity.EmployeeSkill;
import com.zodiakk.employeeservice.employee.entity.Grade;
import com.zodiakk.employeeservice.employee.entity.Position;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Catalog controllers")
class CatalogControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/employees";

    @Nested
    @DisplayName("Departments")
    class Departments {

        @Test
        @DisplayName("supports the full catalog lifecycle")
        void shouldManageDepartmentLifecycle() throws Exception {
            UUID id = createdId(mockMvc.perform(asAdmin(post(BASE_URL + "/departments"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(DepartmentCreateRequestDto.builder()
                            .name("Backend")
                            .description("Backend development")
                            .build()))));

            mockMvc.perform(asHr(get(BASE_URL + "/departments/" + id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Backend"))
                    .andExpect(jsonPath("$.active").value(true));

            mockMvc.perform(asHr(get(BASE_URL + "/departments").param("name", "back")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            mockMvc.perform(asAdmin(put(BASE_URL + "/departments/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(DepartmentUpdateRequestDto.builder()
                                    .name("Platform")
                                    .description("Platform engineering")
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Platform"));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/departments/" + id + "/deactivate")))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asHr(get(BASE_URL + "/departments/" + id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/departments/" + id + "/activate")))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asAdmin(delete(BASE_URL + "/departments/" + id)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asHr(get(BASE_URL + "/departments/" + id)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("rejects a duplicate name")
        void shouldRejectDuplicateName() throws Exception {
            persistDepartment("Backend");

            mockMvc.perform(asAdmin(post(BASE_URL + "/departments"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(DepartmentCreateRequestDto.builder()
                                    .name("backend")
                                    .build())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects deletion of a department used by an employee")
        void shouldRejectDepartmentInUse() throws Exception {
            Department department = persistDepartment("Backend");
            Employee employee = persistEmployee(
                    persistPosition("Developer"),
                    persistGrade("Lead", 4)
            );
            employee.setDepartment(department);
            employeeRepository.saveAndFlush(employee);

            mockMvc.perform(asAdmin(delete(BASE_URL + "/departments/" + department.getId())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects a blank name")
        void shouldRejectBlankName() throws Exception {
            mockMvc.perform(asAdmin(post(BASE_URL + "/departments"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(DepartmentCreateRequestDto.builder()
                                    .name(" ")
                                    .build())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 for unknown identifiers")
        void shouldReturnNotFound() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(asHr(get(BASE_URL + "/departments/" + id))).andExpect(status().isNotFound());
            mockMvc.perform(asAdmin(delete(BASE_URL + "/departments/" + id))).andExpect(status().isNotFound());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/departments/" + id + "/activate")))
                    .andExpect(status().isNotFound());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/departments/" + id + "/deactivate")))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Positions")
    class Positions {

        @Test
        @DisplayName("supports the full catalog lifecycle")
        void shouldManagePositionLifecycle() throws Exception {
            UUID id = createdId(mockMvc.perform(asAdmin(post(BASE_URL + "/positions"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(PositionCreateRequestDto.builder()
                            .name("Software Engineer")
                            .description("Writes code")
                            .build()))));

            mockMvc.perform(asEmployee(get(BASE_URL + "/positions/" + id), UUID.randomUUID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Software Engineer"));

            mockMvc.perform(asHr(get(BASE_URL + "/positions")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            mockMvc.perform(asAdmin(put(BASE_URL + "/positions/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(PositionUpdateRequestDto.builder()
                                    .name("Senior Software Engineer")
                                    .active(true)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Senior Software Engineer"));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/positions/" + id + "/deactivate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/positions/" + id + "/activate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(delete(BASE_URL + "/positions/" + id)))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asHr(get(BASE_URL + "/positions/" + id)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("rejects a duplicate name and an in-use position")
        void shouldRejectDuplicateAndInUse() throws Exception {
            Position position = persistPosition("Software Engineer");
            persistEmployee(position, persistGrade("Lead", 4));

            mockMvc.perform(asAdmin(post(BASE_URL + "/positions"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(PositionCreateRequestDto.builder()
                                    .name("software engineer")
                                    .build())))
                    .andExpect(status().isConflict());

            mockMvc.perform(asAdmin(delete(BASE_URL + "/positions/" + position.getId())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects update without the active flag")
        void shouldRequireActiveOnUpdate() throws Exception {
            Position position = persistPosition("Software Engineer");

            mockMvc.perform(asAdmin(put(BASE_URL + "/positions/" + position.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(PositionUpdateRequestDto.builder()
                                    .name("Architect")
                                    .build())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Specializations")
    class Specializations {

        @Test
        @DisplayName("supports the full catalog lifecycle")
        void shouldManageSpecializationLifecycle() throws Exception {
            UUID id = createdId(mockMvc.perform(asAdmin(post(BASE_URL + "/specializations"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(SpecializationCreateRequestDto.builder()
                            .name("Java")
                            .description("Java stack")
                            .build()))));

            mockMvc.perform(asHr(get(BASE_URL + "/specializations/" + id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Java"));

            mockMvc.perform(asHr(get(BASE_URL + "/specializations")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            mockMvc.perform(asAdmin(put(BASE_URL + "/specializations/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(SpecializationUpdateRequestDto.builder()
                                    .name("Kotlin")
                                    .active(true)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Kotlin"));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/specializations/" + id + "/deactivate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/specializations/" + id + "/activate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(delete(BASE_URL + "/specializations/" + id)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("rejects duplicate and in-use specializations")
        void shouldRejectDuplicateAndInUse() throws Exception {
            Specialization specialization = persistSpecialization("Java");
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            Employee employee = persistEmployee(position, grade);
            employee.setSpecialization(specialization);
            employeeRepository.saveAndFlush(employee);

            mockMvc.perform(asAdmin(post(BASE_URL + "/specializations"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(SpecializationCreateRequestDto.builder()
                                    .name("JAVA")
                                    .build())))
                    .andExpect(status().isConflict());

            mockMvc.perform(asAdmin(delete(BASE_URL + "/specializations/" + specialization.getId())))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Grades")
    class Grades {

        @Test
        @DisplayName("supports the full catalog lifecycle")
        void shouldManageGradeLifecycle() throws Exception {
            UUID id = createdId(mockMvc.perform(asAdmin(post(BASE_URL + "/grades"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(GradeCreateRequestDto.builder()
                            .name("Senior")
                            .level(3)
                            .description("Senior grade")
                            .build()))));

            mockMvc.perform(asHr(get(BASE_URL + "/grades/" + id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.level").value(3));

            mockMvc.perform(asHr(get(BASE_URL + "/grades").param("level", "3")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            mockMvc.perform(asAdmin(put(BASE_URL + "/grades/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(GradeUpdateRequestDto.builder()
                                    .name("Lead")
                                    .level(4)
                                    .active(true)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Lead"))
                    .andExpect(jsonPath("$.level").value(4));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/grades/" + id + "/deactivate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/grades/" + id + "/activate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(delete(BASE_URL + "/grades/" + id)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("rejects an invalid level")
        void shouldRejectInvalidLevel() throws Exception {
            mockMvc.perform(asAdmin(post(BASE_URL + "/grades"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(GradeCreateRequestDto.builder()
                                    .name("Invalid")
                                    .level(0)
                                    .build())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects duplicate and in-use grades")
        void shouldRejectDuplicateAndInUse() throws Exception {
            Grade grade = persistGrade("Senior", 3);
            persistEmployee(persistPosition("Developer"), grade);

            mockMvc.perform(asAdmin(post(BASE_URL + "/grades"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(GradeCreateRequestDto.builder()
                                    .name("senior")
                                    .level(3)
                                    .build())))
                    .andExpect(status().isConflict());

            mockMvc.perform(asAdmin(delete(BASE_URL + "/grades/" + grade.getId())))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Skills")
    class Skills {

        @Test
        @DisplayName("supports the full catalog lifecycle")
        void shouldManageSkillLifecycle() throws Exception {
            UUID id = createdId(mockMvc.perform(asAdmin(post(BASE_URL + "/skills"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(SkillCreateRequestDto.builder()
                            .name("Java")
                            .category("Backend")
                            .description("JVM language")
                            .build()))));

            mockMvc.perform(asHr(get(BASE_URL + "/skills/" + id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.category").value("Backend"));

            mockMvc.perform(asHr(get(BASE_URL + "/skills").param("category", "Backend")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            mockMvc.perform(asAdmin(put(BASE_URL + "/skills/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(SkillUpdateRequestDto.builder()
                                    .name("Kotlin")
                                    .category("Backend")
                                    .active(true)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Kotlin"));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/skills/" + id + "/deactivate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/skills/" + id + "/activate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(delete(BASE_URL + "/skills/" + id)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("rejects duplicate and in-use skills")
        void shouldRejectDuplicateAndInUse() throws Exception {
            Skill skill = persistSkill("Java");
            Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));
            employeeSkillRepository.saveAndFlush(EmployeeSkill.builder()
                    .employee(employee)
                    .skill(skill)
                    .level(4)
                    .build());

            mockMvc.perform(asAdmin(post(BASE_URL + "/skills"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(SkillCreateRequestDto.builder()
                                    .name("java")
                                    .build())))
                    .andExpect(status().isConflict());

            mockMvc.perform(asAdmin(delete(BASE_URL + "/skills/" + skill.getId())))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Responsibility types")
    class ResponsibilityTypes {

        @Test
        @DisplayName("supports the full catalog lifecycle")
        void shouldManageResponsibilityTypeLifecycle() throws Exception {
            UUID id = createdId(mockMvc.perform(asAdmin(post(BASE_URL + "/responsibility-types"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(ResponsibilityTypeCreateRequestDto.builder()
                            .code("MENTOR")
                            .name("Mentor")
                            .description("Mentors new joiners")
                            .multipleAllowed(true)
                            .build()))));

            mockMvc.perform(asHr(get(BASE_URL + "/responsibility-types/" + id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("MENTOR"))
                    .andExpect(jsonPath("$.multipleAllowed").value(true));

            mockMvc.perform(asHr(get(BASE_URL + "/responsibility-types").param("code", "MENT")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            mockMvc.perform(asAdmin(put(BASE_URL + "/responsibility-types/" + id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(ResponsibilityTypeUpdateRequestDto.builder()
                                    .code("MENTOR")
                                    .name("Senior mentor")
                                    .multipleAllowed(false)
                                    .active(true)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Senior mentor"))
                    .andExpect(jsonPath("$.multipleAllowed").value(false));

            mockMvc.perform(asAdmin(patch(BASE_URL + "/responsibility-types/" + id + "/deactivate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(patch(BASE_URL + "/responsibility-types/" + id + "/activate")))
                    .andExpect(status().isNoContent());
            mockMvc.perform(asAdmin(delete(BASE_URL + "/responsibility-types/" + id)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("rejects duplicate and in-use responsibility types")
        void shouldRejectDuplicateAndInUse() throws Exception {
            ResponsibilityType type = persistResponsibilityType("MENTOR");
            Position position = persistPosition("Developer");
            Grade grade = persistGrade("Lead", 4);
            Employee employee = persistEmployee(position, grade);
            Employee manager = persistEmployee(position, grade);
            employeeResponsibilityRepository.saveAndFlush(EmployeeResponsibility.builder()
                    .employee(employee)
                    .responsibleEmployee(manager)
                    .responsibilityType(type)
                    .startDate(LocalDate.now())
                    .build());

            mockMvc.perform(asAdmin(post(BASE_URL + "/responsibility-types"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(ResponsibilityTypeCreateRequestDto.builder()
                                    .code("MENTOR")
                                    .name("Mentor")
                                    .multipleAllowed(true)
                                    .build())))
                    .andExpect(status().isConflict());

            mockMvc.perform(asAdmin(delete(BASE_URL + "/responsibility-types/" + type.getId())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("rejects a missing multipleAllowed flag")
        void shouldRequireMultipleAllowed() throws Exception {
            mockMvc.perform(asAdmin(post(BASE_URL + "/responsibility-types"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(ResponsibilityTypeCreateRequestDto.builder()
                                    .code("MENTOR")
                                    .name("Mentor")
                                    .build())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Authorization and validation")
    class Authorization {

        @Test
        @DisplayName("forbids HR and managers to manage catalogs")
        void shouldForbidNonAdmins() throws Exception {
            String body = toJson(DepartmentCreateRequestDto.builder().name("Backend").build());

            mockMvc.perform(asHr(post(BASE_URL + "/departments"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());

            mockMvc.perform(asManager(post(BASE_URL + "/departments"), UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("allows any authenticated user to read catalogs")
        void shouldAllowEmployeeToReadCatalogs() throws Exception {
            Department department = persistDepartment("Backend");

            mockMvc.perform(asEmployee(get(BASE_URL + "/departments/" + department.getId()), UUID.randomUUID()))
                    .andExpect(status().isOk());
            mockMvc.perform(asEmployee(get(BASE_URL + "/departments"), UUID.randomUUID()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("rejects unauthenticated access")
        void shouldRejectAnonymousAccess() throws Exception {
            mockMvc.perform(get(BASE_URL + "/departments"))
                    .andExpect(status().isUnauthorized());
        }
    }

    private UUID createdId(ResultActions actions) throws Exception {
        String location = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(location).get("id").asString());
    }
}
