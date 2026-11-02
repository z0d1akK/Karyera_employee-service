package com.zodiakk.employeeservice.integration.employee.service;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.integration.employee.dto.EmployeePositionChangedEvent;
import com.zodiakk.employeeservice.integration.employee.dto.EmployeeSkillUpdatedEvent;
import com.zodiakk.employeeservice.outbox.service.OutboxEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeEventServiceTest {

    private static final String TOPIC = "employee.events.v1";

    private static final UUID EMPLOYEE_ID = UUID.fromString("abababab-cdcd-efef-0101-232323232323");

    private static final UUID SKILL_ID = UUID.fromString("cdcdcdcd-efef-0101-2323-454545454545");

    private static final UUID PREVIOUS_POSITION_ID = UUID.fromString("01010101-2323-4545-6767-898989898989");

    private static final UUID PREVIOUS_GRADE_ID = UUID.fromString("02020202-2424-4646-6868-909090909090");

    private static final UUID POSITION_ID = UUID.fromString("03030303-2525-4747-6969-919191919191");

    private static final UUID GRADE_ID = UUID.fromString("04040404-2626-4848-7070-929292929292");

    @Mock
    private OutboxEventService outboxEventService;

    private EmployeeEventService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeEventService(outboxEventService);
        ReflectionTestUtils.setField(service, "employeeEventsTopic", TOPIC);
    }

    @Nested
    @DisplayName("recordSkillUpdated")
    class RecordSkillUpdated {

        @Test
        @DisplayName("saves an EmployeeSkillUpdated outbox event for the employee aggregate")
        void shouldSaveOutboxEvent() {
            LocalDate assessedAt = LocalDate.of(2026, 10, 1);

            service.recordSkillUpdated(
                    EMPLOYEE_ID,
                    SKILL_ID,
                    4,
                    new BigDecimal("3.5"),
                    assessedAt,
                    KafkaEventConstants.SKILL_CHANGE_UPDATED
            );

            ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

            verify(outboxEventService).saveEvent(
                    eq(TOPIC),
                    eq(EMPLOYEE_ID),
                    eq(KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE),
                    eq(KafkaEventConstants.EMPLOYEE_SKILL_UPDATED),
                    eq(KafkaEventConstants.EVENT_VERSION_V1),
                    payloadCaptor.capture()
            );

            assertThat(payloadCaptor.getValue()).isEqualTo(new EmployeeSkillUpdatedEvent(
                    EMPLOYEE_ID,
                    SKILL_ID,
                    4,
                    new BigDecimal("3.5"),
                    assessedAt,
                    KafkaEventConstants.SKILL_CHANGE_UPDATED
            ));
        }
    }

    @Nested
    @DisplayName("recordPositionChanged")
    class RecordPositionChanged {

        @Test
        @DisplayName("saves an EmployeePositionChanged outbox event for the employee aggregate")
        void shouldSaveOutboxEvent() {
            service.recordPositionChanged(
                    EMPLOYEE_ID,
                    PREVIOUS_POSITION_ID,
                    PREVIOUS_GRADE_ID,
                    POSITION_ID,
                    GRADE_ID
            );

            ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

            verify(outboxEventService).saveEvent(
                    eq(TOPIC),
                    eq(EMPLOYEE_ID),
                    eq(KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE),
                    eq(KafkaEventConstants.EMPLOYEE_POSITION_CHANGED),
                    eq(KafkaEventConstants.EVENT_VERSION_V1),
                    payloadCaptor.capture()
            );

            assertThat(payloadCaptor.getValue()).isEqualTo(new EmployeePositionChangedEvent(
                    EMPLOYEE_ID,
                    PREVIOUS_POSITION_ID,
                    PREVIOUS_GRADE_ID,
                    POSITION_ID,
                    GRADE_ID
            ));
        }
    }
}
