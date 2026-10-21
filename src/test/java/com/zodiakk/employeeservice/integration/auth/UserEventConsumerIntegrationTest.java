package com.zodiakk.employeeservice.integration.auth;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.integration.auth.dto.UserCreatedEvent;
import com.zodiakk.employeeservice.integration.auth.dto.UserDeletedEvent;
import com.zodiakk.employeeservice.integration.auth.dto.UserEventEnvelope;
import com.zodiakk.employeeservice.outbox.entity.OutboxEvent;
import com.zodiakk.employeeservice.outbox.entity.OutboxEventStatus;
import com.zodiakk.employeeservice.outbox.publisher.OutboxPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User event consumer and outbox")
class UserEventConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(45);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    @Value("${app.kafka.topics.employee-account-events}")
    private String employeeAccountEventsTopic;

    @Test
    @DisplayName("links an employee account from a Kafka event")
    void shouldLinkAccountFromKafka() throws Exception {
        Employee employee = newEmployee();
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        send(userId, eventId, KafkaEventConstants.USER_CREATED,
                new UserCreatedEvent(userId, employee.getId()));

        await("employee account link", () -> employeeRepository.findById(employee.getId())
                .map(Employee::getUserId)
                .filter(userId::equals)
                .isPresent());

        assertThat(processedEventRepository.findById(eventId)).isPresent();
    }

    @Test
    @DisplayName("unlinks an account, records the unlink and publishes an outbox event")
    void shouldUnlinkAccountAndPublishOutboxEvent() throws Exception {
        Employee employee = newEmployee();
        UUID userId = UUID.randomUUID();
        UUID linkEventId = UUID.randomUUID();

        send(userId, linkEventId, KafkaEventConstants.USER_CREATED,
                new UserCreatedEvent(userId, employee.getId()));

        await("employee account link", () -> employeeRepository.findById(employee.getId())
                .map(Employee::getUserId)
                .filter(userId::equals)
                .isPresent());

        UUID unlinkEventId = UUID.randomUUID();
        send(userId, unlinkEventId, KafkaEventConstants.USER_DELETED,
                new UserDeletedEvent(userId, employee.getId()));

        await("employee account unlink", () -> employeeRepository.findById(employee.getId())
                .map(Employee::getUserId)
                .isEmpty());

        assertThat(employeeAccountUnlinkRepository.existsById(userId)).isTrue();
        assertThat(processedEventRepository.findById(unlinkEventId)).isPresent();

        OutboxEvent outboxEvent = outboxEventRepository.findAll()
                .stream()
                .filter(event -> KafkaEventConstants.EMPLOYEE_ACCOUNT_UNLINKED
                        .equals(event.getEventType()))
                .findFirst()
                .orElseThrow();

        assertThat(outboxEvent.getAggregateId()).isEqualTo(employee.getId());
        assertThat(outboxEvent.getAggregateType())
                .isEqualTo(KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE_ACCOUNT);
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(outboxEvent.getPayload()).contains(userId.toString());

        outboxPublisher.publishPendingEvents();

        await("outbox event publication", () -> outboxEventRepository
                .findById(outboxEvent.getId())
                .map(event -> event.getStatus() == OutboxEventStatus.PUBLISHED)
                .orElse(false));
    }

    @Test
    @DisplayName("unlinks an account recorded by the auth service")
    void shouldUnlinkAccountRecordedByAuthService() throws Exception {
        Employee employee = newEmployee();
        UUID userId = UUID.randomUUID();

        send(userId, UUID.randomUUID(), KafkaEventConstants.USER_DELETED,
                new UserDeletedEvent(userId, employee.getId()));

        await("employee account unlink", () ->
                employeeAccountUnlinkRepository.existsById(userId));

        assertThat(outboxEventRepository.findAll())
                .anyMatch(event -> KafkaEventConstants.EMPLOYEE_ACCOUNT_UNLINKED
                        .equals(event.getEventType()));
    }

    private Employee newEmployee() {
        return persistUnlinkedEmployee(
                persistPosition("Developer-" + UUID.randomUUID()),
                persistGrade("Lead-" + UUID.randomUUID(), nextGradeLevel())
        );
    }

    private void send(UUID key, UUID eventId, String type, Object payload) throws Exception {
        String message = objectMapper.writeValueAsString(new UserEventEnvelope(
                eventId,
                type,
                KafkaEventConstants.EVENT_VERSION_V1,
                OffsetDateTime.now(ZoneOffset.UTC),
                objectMapper.valueToTree(payload)
        ));

        kafkaTemplate.send(userEventsTopic, key.toString(), message)
                .get(15, TimeUnit.SECONDS);
    }

    private void await(String description, BooleanSupplier condition) {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();

        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }

        throw new AssertionError("Timed out waiting for " + description);
    }
}
