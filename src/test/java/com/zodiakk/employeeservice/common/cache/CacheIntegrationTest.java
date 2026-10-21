package com.zodiakk.employeeservice.common.cache;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Redis caching")
class CacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("caches catalog entries and evicts them on update")
    void shouldCacheCatalogEntries() throws Exception {
        Department department = persistDepartment("Backend");

        mockMvc.perform(asHr(get("/api/employees/departments/" + department.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Backend"));

        Cache cache = cacheManager.getCache(CacheNames.DEPARTMENTS);
        assertThat(cache).isNotNull();
        assertThat(cache.get(department.getId())).isNotNull();

        Set<String> keys = stringRedisTemplate.keys("employee-service:*");
        assertThat(keys).isNotNull().anyMatch(key -> key.contains("departments"));

        mockMvc.perform(asAdmin(put("/api/employees/departments/" + department.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(DepartmentUpdateRequestDto.builder()
                                .name("Platform")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Platform"));

        assertThat(cache.get(department.getId())).isNull();
    }

    @Test
    @DisplayName("caches employee profiles by id and user id and evicts them on update")
    void shouldCacheEmployeeProfiles() throws Exception {
        Employee employee = persistEmployee(persistPosition("Developer"), persistGrade("Lead", 4));

        mockMvc.perform(asHr(get("/api/employees/" + employee.getId())))
                .andExpect(status().isOk());
        mockMvc.perform(asHr(get("/api/employees/user/" + employee.getUserId())))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache(CacheNames.EMPLOYEES);
        assertThat(cache).isNotNull();
        assertThat(cache.get("id:" + employee.getId())).isNotNull();
        assertThat(cache.get("user:" + employee.getUserId())).isNotNull();

        mockMvc.perform(asHr(put("/api/employees/" + employee.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(EmployeeUpdateRequestDto.builder()
                                .firstName("Sergey")
                                .email(employee.getEmail())
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Sergey"));

        assertThat(cache.get("id:" + employee.getId())).isNull();
        assertThat(cache.get("user:" + employee.getUserId())).isNull();
    }
}
