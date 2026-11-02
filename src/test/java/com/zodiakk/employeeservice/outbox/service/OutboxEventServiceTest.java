package com.zodiakk.employeeservice.outbox.service;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.integration.auth.dto.EmployeeAccountUnlinkedEvent;
import com.zodiakk.employeeservice.outbox.entity.OutboxEvent;
import com.zodiakk.employeeservice.outbox.entity.OutboxEventStatus;
import com.zodiakk.employeeservice.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    private static final UUID AGGREGATE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID EMPLOYEE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final String TOPIC = "employee.account.events.v1";

    private static final String AGGREGATE_TYPE = "EMPLOYEE_ACCOUNT";

    private static final String EVENT_TYPE = "EMPLOYEE_ACCOUNT_UNLINKED";

    private static final int EVENT_VERSION = 1;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Nested
    @DisplayName("saveEvent")
    class SaveEvent {

        @Test
        @DisplayName("serializes the envelope and persists a pending outbox event")
        void shouldPersistPendingEvent() {
            ObjectMapper objectMapper = new ObjectMapper();
            OutboxEventService service = new OutboxEventService(outboxEventRepository, objectMapper);
            EmployeeAccountUnlinkedEvent payload =
                    new EmployeeAccountUnlinkedEvent(USER_ID, EMPLOYEE_ID);

            service.saveEvent(TOPIC, AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, EVENT_VERSION, payload);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());

            OutboxEvent saved = captor.getValue();

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTopic()).isEqualTo(TOPIC);
            assertThat(saved.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(saved.getAggregateType()).isEqualTo(AGGREGATE_TYPE);
            assertThat(saved.getEventType()).isEqualTo(EVENT_TYPE);
            assertThat(saved.getEventVersion()).isEqualTo(EVENT_VERSION);
            assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
            assertThat(saved.getAttempts()).isZero();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getNextAttemptAt()).isEqualTo(saved.getCreatedAt());
            assertThat(saved.getPublishedAt()).isNull();
            assertThat(saved.getLockedUntil()).isNull();

            JsonNode envelope = objectMapper.readTree(saved.getPayload());

            assertThat(envelope.get("eventId").asString()).isEqualTo(saved.getId().toString());
            assertThat(envelope.get("aggregateId").asString()).isEqualTo(AGGREGATE_ID.toString());
            assertThat(envelope.get("aggregateType").asString()).isEqualTo(AGGREGATE_TYPE);
            assertThat(envelope.get("eventType").asString()).isEqualTo(EVENT_TYPE);
            assertThat(envelope.get("eventVersion").asInt()).isEqualTo(EVENT_VERSION);
            assertThat(envelope.get("occurredAt").asString()).isNotBlank();
            assertThat(envelope.get("payload").get("userId").asString())
                    .isEqualTo(USER_ID.toString());
            assertThat(envelope.get("payload").get("employeeId").asString())
                    .isEqualTo(EMPLOYEE_ID.toString());
        }

        @Test
        @DisplayName("wraps Jackson serialization failures in IllegalStateException")
        void shouldWrapSerializationFailure() {
            JacksonException failure = new StreamConstraintsException("serialization boom");
            ObjectMapper objectMapper = spy(new ObjectMapper());
            doThrow(failure).when(objectMapper).writeValueAsString(any());

            OutboxEventService service = new OutboxEventService(outboxEventRepository, objectMapper);

            assertThatThrownBy(() -> service.saveEvent(
                    TOPIC,
                    AGGREGATE_ID,
                    AGGREGATE_TYPE,
                    EVENT_TYPE,
                    EVENT_VERSION,
                    new EmployeeAccountUnlinkedEvent(USER_ID, EMPLOYEE_ID)
            ))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(ErrorMessages.OUTBOX_EVENT_SERIALIZATION_FAILED.formatted(EVENT_TYPE))
                    .hasCauseInstanceOf(StreamConstraintsException.class);

            verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("propagates non-Jackson runtime failures unchanged")
        void shouldPropagateOtherRuntimeFailures() {
            IllegalStateException failure = new IllegalStateException("unexpected mapper failure");
            ObjectMapper objectMapper = spy(new ObjectMapper());
            doThrow(failure).when(objectMapper).writeValueAsString(any());

            OutboxEventService service = new OutboxEventService(outboxEventRepository, objectMapper);

            assertThatThrownBy(() -> service.saveEvent(
                    TOPIC,
                    AGGREGATE_ID,
                    AGGREGATE_TYPE,
                    EVENT_TYPE,
                    EVENT_VERSION,
                    new EmployeeAccountUnlinkedEvent(USER_ID, EMPLOYEE_ID)
            ))
                    .isSameAs(failure);

            verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
        }
    }
}
