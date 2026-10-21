package com.zodiakk.employeeservice.employee;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.dto.request.create.EducationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeSkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.ExperienceCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.create.MilitaryObligationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EducationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeSkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ExperienceUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.MilitaryObligationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.entity.enums.EducationLevel;
import com.zodiakk.employeeservice.employee.entity.enums.MilitaryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Employee profile sub-resources")
class EmployeeProfileIntegrationTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("Education")
    class Education {

        @Test
        @DisplayName("supports the full education lifecycle")
        void shouldManageEducation() throws Exception {
            Employee employee = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/education";

            UUID educationId = idOf(mockMvc.perform(asHr(post(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(EducationCreateRequestDto.builder()
                            .educationLevel(EducationLevel.BACHELOR)
                            .institutionName("BSUIR")
                            .fieldOfStudy("Computer Science")
                            .degreeName("Engineer")
                            .startDate(LocalDate.of(2014, 9, 1))
                            .endDate(LocalDate.of(2018, 6, 30))
                            .build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.educationLevel").value("BACHELOR"))
                    .andExpect(jsonPath("$.institutionName").value("BSUIR")));

            mockMvc.perform(asHr(get(url + "/" + educationId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(educationId.toString()));

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            mockMvc.perform(asHr(put(url + "/" + educationId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EducationUpdateRequestDto.builder()
                                    .educationLevel(EducationLevel.MASTER)
                                    .institutionName("BSU")
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.educationLevel").value("MASTER"))
                    .andExpect(jsonPath("$.institutionName").value("BSU"));

            mockMvc.perform(asHr(delete(url + "/" + educationId)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asHr(get(url + "/" + educationId)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("lets an employee manage his own education")
        void shouldLetEmployeeManageOwnEducation() throws Exception {
            Employee employee = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/education";

            mockMvc.perform(asEmployee(post(url), employee.getUserId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EducationCreateRequestDto.builder()
                                    .educationLevel(EducationLevel.SECONDARY)
                                    .institutionName("School 1")
                                    .build())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("rejects invalid dates, foreign records and unknown employees")
        void shouldRejectInvalidEducationRequests() throws Exception {
            Employee employee = newEmployee();
            Employee other = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/education";

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EducationCreateRequestDto.builder()
                                    .educationLevel(EducationLevel.BACHELOR)
                                    .institutionName("BSUIR")
                                    .startDate(LocalDate.of(2020, 1, 1))
                                    .endDate(LocalDate.of(2019, 1, 1))
                                    .build())))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"institutionName\":\"BSUIR\"}"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(post("/api/employees/" + UUID.randomUUID() + "/education"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EducationCreateRequestDto.builder()
                                    .educationLevel(EducationLevel.BACHELOR)
                                    .institutionName("BSUIR")
                                    .build())))
                    .andExpect(status().isNotFound());

            UUID educationId = idOf(mockMvc.perform(asHr(post(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(EducationCreateRequestDto.builder()
                            .educationLevel(EducationLevel.BACHELOR)
                            .institutionName("BSUIR")
                            .build())))
                    .andExpect(status().isCreated()));

            mockMvc.perform(asHr(get("/api/employees/" + other.getId() + "/education/" + educationId)))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(asHr(delete("/api/employees/" + other.getId() + "/education/" + educationId)))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(asHr(get(url + "/" + UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("forbids a foreign employee to add education")
        void shouldForbidForeignEducationAccess() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asEmployee(post("/api/employees/" + employee.getId() + "/education"),
                            UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EducationCreateRequestDto.builder()
                                    .educationLevel(EducationLevel.BACHELOR)
                                    .institutionName("BSUIR")
                                    .build())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Experience")
    class Experience {

        @Test
        @DisplayName("supports the full experience lifecycle")
        void shouldManageExperience() throws Exception {
            Employee employee = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/experience";

            UUID experienceId = idOf(mockMvc.perform(asHr(post(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(ExperienceCreateRequestDto.builder()
                            .company("EPAM")
                            .position("Software Engineer")
                            .description("Backend development")
                            .startDate(LocalDate.of(2019, 1, 1))
                            .endDate(LocalDate.of(2022, 1, 1))
                            .build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.company").value("EPAM")));

            mockMvc.perform(asHr(get(url + "/" + experienceId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.position").value("Software Engineer"));

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            mockMvc.perform(asHr(put(url + "/" + experienceId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(ExperienceUpdateRequestDto.builder()
                                    .company("Google")
                                    .position("Senior Engineer")
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.company").value("Google"));

            mockMvc.perform(asHr(delete(url + "/" + experienceId)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("rejects invalid experience requests")
        void shouldRejectInvalidExperienceRequests() throws Exception {
            Employee employee = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/experience";

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(ExperienceCreateRequestDto.builder()
                                    .company("EPAM")
                                    .position("Engineer")
                                    .startDate(LocalDate.now().plusDays(5))
                                    .build())))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(ExperienceCreateRequestDto.builder()
                                    .company("EPAM")
                                    .position("Engineer")
                                    .startDate(LocalDate.of(2022, 1, 1))
                                    .endDate(LocalDate.of(2021, 1, 1))
                                    .build())))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(get("/api/employees/" + UUID.randomUUID() + "/experience")))
                    .andExpect(status().isNotFound());

            mockMvc.perform(asHr(get(url + "/" + UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Skills")
    class Skills {

        @Test
        @DisplayName("supports the full employee skill lifecycle")
        void shouldManageEmployeeSkills() throws Exception {
            Employee employee = newEmployee();
            Skill skill = persistSkill("Java");
            String url = "/api/employees/" + employee.getId() + "/skills";

            UUID employeeSkillId = idOf(mockMvc.perform(asHr(post(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(EmployeeSkillCreateRequestDto.builder()
                            .skillId(skill.getId())
                            .level(3)
                            .yearsOfExperience(new BigDecimal("2.5"))
                            .build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.level").value(3))
                    .andExpect(jsonPath("$.skill.id").value(skill.getId().toString())));

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            mockMvc.perform(asHr(put(url + "/" + employeeSkillId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillUpdateRequestDto.builder()
                                    .level(5)
                                    .yearsOfExperience(new BigDecimal("4.0"))
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.level").value(5))
                    .andExpect(jsonPath("$.yearsOfExperience").value(4.0));

            mockMvc.perform(asHr(delete(url + "/" + employeeSkillId)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("rejects duplicate, invalid and foreign skill assignments")
        void shouldRejectInvalidSkillAssignments() throws Exception {
            Employee employee = newEmployee();
            Employee other = newEmployee();
            Skill skill = persistSkill("Java");
            Skill inactiveSkill = persistSkill("COBOL", false);
            String url = "/api/employees/" + employee.getId() + "/skills";

            UUID employeeSkillId = idOf(mockMvc.perform(asHr(post(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(EmployeeSkillCreateRequestDto.builder()
                            .skillId(skill.getId())
                            .level(3)
                            .build())))
                    .andExpect(status().isCreated()));

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillCreateRequestDto.builder()
                                    .skillId(skill.getId())
                                    .level(4)
                                    .build())))
                    .andExpect(status().isConflict());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillCreateRequestDto.builder()
                                    .skillId(inactiveSkill.getId())
                                    .level(4)
                                    .build())))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillCreateRequestDto.builder()
                                    .skillId(skill.getId())
                                    .level(9)
                                    .build())))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(post("/api/employees/" + UUID.randomUUID() + "/skills"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillCreateRequestDto.builder()
                                    .skillId(skill.getId())
                                    .level(3)
                                    .build())))
                    .andExpect(status().isNotFound());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillCreateRequestDto.builder()
                                    .skillId(UUID.randomUUID())
                                    .level(3)
                                    .build())))
                    .andExpect(status().isNotFound());

            mockMvc.perform(asHr(put("/api/employees/" + other.getId() + "/skills/" + employeeSkillId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(EmployeeSkillUpdateRequestDto.builder().level(4).build())))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(delete("/api/employees/" + other.getId() + "/skills/" + employeeSkillId)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 for an unknown employee when listing skills")
        void shouldReturnNotFoundForUnknownEmployee() throws Exception {
            mockMvc.perform(asHr(get("/api/employees/" + UUID.randomUUID() + "/skills")))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Military obligation")
    class MilitaryObligation {

        @Test
        @DisplayName("supports the full military obligation lifecycle")
        void shouldManageMilitaryObligation() throws Exception {
            Employee employee = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/military-obligation";

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(MilitaryObligationCreateRequestDto.builder()
                                    .status(MilitaryStatus.LIABLE)
                                    .category("A")
                                    .documentNumber("AB123456")
                                    .build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("LIABLE"))
                    .andExpect(jsonPath("$.documentNumber").value("AB123456"));

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("LIABLE"));

            mockMvc.perform(asHr(put(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(MilitaryObligationCreateRequestDto.builder()
                                    .status(MilitaryStatus.COMPLETED)
                                    .militaryRank("Private")
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.militaryRank").value("Private"));

            mockMvc.perform(asHr(patch(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(MilitaryObligationUpdateRequestDto.builder()
                                    .status(MilitaryStatus.EXEMPT)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("EXEMPT"));

            mockMvc.perform(asHr(delete(url)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(asHr(get(url)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("creates the obligation on upsert when it is missing")
        void shouldCreateOnUpsert() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asHr(put("/api/employees/" + employee.getId() + "/military-obligation"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(MilitaryObligationCreateRequestDto.builder()
                                    .status(MilitaryStatus.DEFERRED)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DEFERRED"));
        }

        @Test
        @DisplayName("rejects a duplicate obligation and invalid requests")
        void shouldRejectInvalidMilitaryObligationRequests() throws Exception {
            Employee employee = newEmployee();
            String url = "/api/employees/" + employee.getId() + "/military-obligation";
            MilitaryObligationCreateRequestDto request = MilitaryObligationCreateRequestDto.builder()
                    .status(MilitaryStatus.LIABLE)
                    .build();

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict());

            mockMvc.perform(asHr(post(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(asHr(get("/api/employees/" + UUID.randomUUID() + "/military-obligation")))
                    .andExpect(status().isNotFound());

            mockMvc.perform(asHr(delete("/api/employees/" + UUID.randomUUID() + "/military-obligation")))
                    .andExpect(status().isNotFound());

            mockMvc.perform(asHr(patch(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(MilitaryObligationUpdateRequestDto.builder()
                                    .status(MilitaryStatus.EXEMPT)
                                    .build())))
                    .andExpect(status().isOk());
        }
    }

    private Employee newEmployee() {
        return persistEmployee(
                persistPosition("Developer-" + UUID.randomUUID()),
                persistGrade("Lead-" + UUID.randomUUID(), nextGradeLevel())
        );
    }

    private UUID idOf(ResultActions actions) throws Exception {
        String body = actions.andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }
}
