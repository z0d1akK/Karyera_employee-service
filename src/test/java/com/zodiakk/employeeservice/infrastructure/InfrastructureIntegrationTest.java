package com.zodiakk.employeeservice.infrastructure;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test infrastructure")
class InfrastructureIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PostgreSQLContainer postgresContainer;

    @Autowired
    private KafkaContainer kafkaContainer;

    @Autowired
    @Qualifier("redisContainer")
    private GenericContainer<?> redisContainer;

    @Test
    @DisplayName("uses a PostgreSQL 17 container with the full Liquibase schema")
    void shouldRunPostgres17() {
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(postgresContainer.getDockerImageName()).startsWith("postgres:17");

        String version = jdbcTemplate.queryForObject("select version()", String.class);
        assertThat(version).contains("PostgreSQL 17");

        assertThat(jdbcTemplate.queryForObject("select current_database()", String.class))
                .isEqualTo("karyera_employee_service_db");

        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'",
                String.class
        );

        assertThat(tables).contains(
                "departments",
                "positions",
                "specializations",
                "grades",
                "skills",
                "responsibility_types",
                "employees",
                "employee_skills",
                "experiences",
                "educations",
                "military_obligations",
                "employee_responsibilities",
                "employee_profile_photos",
                "processed_events",
                "employee_account_unlinks",
                "outbox_events"
        );
    }

    @Test
    @DisplayName("runs an Apache Kafka container that accepts a cluster description")
    void shouldRunKafka() throws Exception {
        assertThat(kafkaContainer.isRunning()).isTrue();
        assertThat(kafkaContainer.getDockerImageName()).startsWith("apache/kafka:4.3.1");
        assertThat(kafkaContainer.getBootstrapServers()).isNotBlank();

        try (AdminClient adminClient = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaContainer.getBootstrapServers()
        ))) {
            String clusterId = adminClient.describeCluster()
                    .clusterId()
                    .get(15, TimeUnit.SECONDS);

            assertThat(clusterId).isNotBlank();
        }
    }

    @Test
    @DisplayName("runs a Redis container reachable over the wire")
    void shouldRunRedis() throws Exception {
        assertThat(redisContainer.isRunning()).isTrue();
        assertThat(redisContainer.getDockerImageName()).startsWith("redis:8.6.2");

        assertThat(pingRedis()).isEqualTo("+PONG");
    }

    @Test
    @DisplayName("starts containers from the pinned images")
    void shouldStartContainers() {
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(kafkaContainer.isRunning()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();

        assertThat(postgresContainer.getDockerImageName()).contains("postgres:17");
        assertThat(kafkaContainer.getDockerImageName()).contains("apache/kafka:4.3.1");
        assertThat(redisContainer.getDockerImageName()).contains("redis:8.6.2");
    }

    private String pingRedis() throws Exception {
        try (Socket socket = new Socket(redisContainer.getHost(),
                redisContainer.getMappedPort(6379))) {
            socket.setSoTimeout(5_000);

            OutputStream output = socket.getOutputStream();
            output.write("PING\r\n".getBytes(StandardCharsets.UTF_8));
            output.flush();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );

            return reader.readLine();
        }
    }
}
