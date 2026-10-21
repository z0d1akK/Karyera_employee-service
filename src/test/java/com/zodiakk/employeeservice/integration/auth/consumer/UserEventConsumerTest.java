package com.zodiakk.employeeservice.integration.auth.consumer;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.integration.auth.service.EmployeeAccountLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    private static final UUID EVENT_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    private static final UUID USER_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    private static final UUID EMPLOYEE_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    @Mock
    private EmployeeAccountLinkService accountLinkService;

    private UserEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserEventConsumer(new ObjectMapper(), accountLinkService);
    }

    @Nested
    @DisplayName("consume")
    class Consume {

        @Test
        @DisplayName("dispatches USER_CREATED to linkAccount with the parsed identifiers")
        void shouldLinkAccountOnUserCreated() {
            String message = message(
                    EVENT_ID,
                    KafkaEventConstants.USER_CREATED,
                    KafkaEventConstants.EVENT_VERSION_V1,
                    userPayload(USER_ID, EMPLOYEE_ID)
            );

            assertThatCode(() -> consumer.consume(message)).doesNotThrowAnyException();

            verify(accountLinkService).linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);
            verifyNoMoreInteractions(accountLinkService);
        }

        @Test
        @DisplayName("dispatches USER_DELETED to unlinkAccount with the parsed identifiers")
        void shouldUnlinkAccountOnUserDeleted() {
            String message = message(
                    EVENT_ID,
                    KafkaEventConstants.USER_DELETED,
                    KafkaEventConstants.EVENT_VERSION_V1,
                    userPayload(USER_ID, EMPLOYEE_ID)
            );

            assertThatCode(() -> consumer.consume(message)).doesNotThrowAnyException();

            verify(accountLinkService).unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);
            verifyNoMoreInteractions(accountLinkService);
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("rejects unsupported event versions before dispatching")
        void shouldRejectUnsupportedVersion() {
            String message = message(
                    EVENT_ID,
                    KafkaEventConstants.USER_CREATED,
                    2,
                    userPayload(USER_ID, EMPLOYEE_ID)
            );

            assertThatThrownBy(() -> consumer.consume(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(ErrorMessages.UNSUPPORTED_EVENT_VERSION.formatted(2));

            verifyNoInteractions(accountLinkService);
        }

        @Test
        @DisplayName("rejects unsupported event types")
        void shouldRejectUnsupportedEventType() {
            String message = message(
                    EVENT_ID,
                    "USER_UPDATED",
                    KafkaEventConstants.EVENT_VERSION_V1,
                    userPayload(USER_ID, EMPLOYEE_ID)
            );

            assertThatThrownBy(() -> consumer.consume(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(ErrorMessages.UNSUPPORTED_EVENT_TYPE.formatted("USER_UPDATED"));

            verifyNoInteractions(accountLinkService);
        }

        @Test
        @DisplayName("propagates malformed JSON so the message can be sent to the DLT")
        void shouldPropagateMalformedJson() {
            String message = "{\"eventId\":\"" + EVENT_ID + "\",\"eventType\":";

            assertThatThrownBy(() -> consumer.consume(message))
                    .isInstanceOf(JacksonException.class);

            verifyNoInteractions(accountLinkService);
        }

        @Test
        @DisplayName("propagates payload conversion failures")
        void shouldPropagatePayloadConversionFailure() {
            String message = message(
                    EVENT_ID,
                    KafkaEventConstants.USER_CREATED,
                    KafkaEventConstants.EVENT_VERSION_V1,
                    "\"not-an-object\""
            );

            assertThatThrownBy(() -> consumer.consume(message))
                    .isInstanceOf(JacksonException.class);

            verifyNoInteractions(accountLinkService);
        }
    }

    private String message(UUID eventId, String eventType, int eventVersion, String payloadJson) {
        return """
                {"eventId":"%s","eventType":"%s","eventVersion":%d,"occurredAt":"2026-01-01T00:00:00Z","payload":%s}
                """.formatted(eventId, eventType, eventVersion, payloadJson);
    }

    private String userPayload(UUID userId, UUID employeeId) {
        return "{\"userId\":\"%s\",\"employeeId\":\"%s\"}".formatted(userId, employeeId);
    }
}
