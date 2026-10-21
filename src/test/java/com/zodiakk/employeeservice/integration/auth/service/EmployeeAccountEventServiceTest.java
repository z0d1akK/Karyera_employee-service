package com.zodiakk.employeeservice.integration.auth.service;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.integration.auth.dto.EmployeeAccountUnlinkedEvent;
import com.zodiakk.employeeservice.outbox.service.OutboxEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeAccountEventServiceTest {

    private static final UUID USER_ID = UUID.fromString("12121212-3434-5656-7878-909090909090");

    private static final UUID EMPLOYEE_ID = UUID.fromString("abababab-cdcd-efef-0101-232323232323");

    @Mock
    private OutboxEventService outboxEventService;

    private EmployeeAccountEventService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeAccountEventService(outboxEventService);
    }

    @Nested
    @DisplayName("recordAccountUnlinked")
    class RecordAccountUnlinked {

        @Test
        @DisplayName("saves an EMPLOYEE_ACCOUNT_UNLINKED outbox event for the employee aggregate")
        void shouldSaveOutboxEvent() {
            service.recordAccountUnlinked(USER_ID, EMPLOYEE_ID);

            ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

            verify(outboxEventService).saveEvent(
                    eq(EMPLOYEE_ID),
                    eq(KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE_ACCOUNT),
                    eq(KafkaEventConstants.EMPLOYEE_ACCOUNT_UNLINKED),
                    eq(KafkaEventConstants.EVENT_VERSION_V1),
                    payloadCaptor.capture()
            );

            assertThat(payloadCaptor.getValue())
                    .isInstanceOf(EmployeeAccountUnlinkedEvent.class)
                    .isEqualTo(new EmployeeAccountUnlinkedEvent(USER_ID, EMPLOYEE_ID));

            EmployeeAccountUnlinkedEvent payload =
                    (EmployeeAccountUnlinkedEvent) payloadCaptor.getValue();
            assertThat(payload.userId()).isEqualTo(USER_ID);
            assertThat(payload.employeeId()).isEqualTo(EMPLOYEE_ID);
        }
    }
}
