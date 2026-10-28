package com.zodiakk.employeeservice.security;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Security")
class SecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("rejects an anonymous request")
    void shouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/v1/employees/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("rejects a request with a malformed user id")
    void shouldRejectMalformedUserId() throws Exception {
        mockMvc.perform(get("/api/v1/employees/" + UUID.randomUUID())
                        .header(USER_ID_HEADER, "not-a-uuid")
                        .header(USER_ROLE_HEADER, "HR")
                        .header(USER_EMAIL_HEADER, "hr@company.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("rejects a request with incomplete gateway headers")
    void shouldRejectIncompleteHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/employees/" + UUID.randomUUID())
                        .header(USER_ID_HEADER, UUID.randomUUID().toString())
                        .header(USER_ROLE_HEADER, "HR"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("rejects an unauthenticated request to a protected write endpoint")
    void shouldRejectAnonymousWrite() throws Exception {
        mockMvc.perform(post("/api/v1/employees/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("serves the health endpoint anonymously")
    void shouldServeHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("allows CORS preflight requests anonymously")
    void shouldAllowOptionsRequests() throws Exception {
        mockMvc.perform(options("/api/v1/employees")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("requires an internal key for the internal API")
    void shouldProtectInternalApi() throws Exception {
        Employee employee = persistEmployee(
                persistPosition("Developer"), persistGrade("Lead", 4)
        );

        mockMvc.perform(get("/internal/v1/employees/{id}/account-info", employee.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/internal/v1/employees/{id}/account-info", employee.getId())
                        .header(INTERNAL_KEY_HEADER, "wrong-key"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(asHr(get("/internal/v1/employees/{id}/account-info", employee.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("returns registration info for the internal service")
    void shouldReturnRegistrationInfoForInternalService() throws Exception {
        Employee employee = persistEmployee(
                persistPosition("Developer"), persistGrade("Lead", 4)
        );

        mockMvc.perform(asInternal(get("/internal/v1/employees/{id}/account-info", employee.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(employee.getId().toString()))
                .andExpect(jsonPath("$.email").value(employee.getEmail()))
                .andExpect(jsonPath("$.employmentStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.userId").value(employee.getUserId().toString()));
    }

    @Test
    @DisplayName("returns 404 for an unknown employee through the internal API")
    void shouldReturnNotFoundForInternalApi() throws Exception {
        mockMvc.perform(asInternal(get("/internal/v1/employees/{id}/account-info", UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("does not accept the internal key on the public API")
    void shouldNotAuthenticatePublicApiWithInternalKey() throws Exception {
        mockMvc.perform(get("/api/v1/employees/" + UUID.randomUUID())
                        .header(INTERNAL_KEY_HEADER, INTERNAL_API_KEY))
                .andExpect(status().isUnauthorized());
    }
}
