package com.zodiakk.employeeservice;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Employee service application context")
class ApplicationContextIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("loads the application context with all infrastructure beans")
    void shouldLoadContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(kafkaTemplate).isNotNull();
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCache("employees")).isNotNull();
        assertThat(cacheManager.getCache("positions")).isNotNull();
        assertThat(cacheManager.getCache("grades")).isNotNull();
        assertThat(cacheManager.getCache("departments")).isNotNull();
        assertThat(cacheManager.getCache("specializations")).isNotNull();
        assertThat(cacheManager.getCache("skills")).isNotNull();
        assertThat(cacheManager.getCache("responsibility-types")).isNotNull();
    }
}
