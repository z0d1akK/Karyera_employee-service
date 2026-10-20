package com.zodiakk.employeeservice.integration.auth.consumer;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.integration.auth.dto.UserCreatedEvent;
import com.zodiakk.employeeservice.integration.auth.dto.UserDeletedEvent;
import com.zodiakk.employeeservice.integration.auth.dto.UserEventEnvelope;
import com.zodiakk.employeeservice.integration.auth.service.EmployeeAccountLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ObjectMapper objectMapper;

    private final EmployeeAccountLinkService accountLinkService;

    @KafkaListener(
            topics = "${app.kafka.topics.user-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        UserEventEnvelope envelope =
                objectMapper.readValue(message, UserEventEnvelope.class);

        if (envelope.eventVersion() != KafkaEventConstants.EVENT_VERSION_V1) {
            throw new IllegalArgumentException(
                    ErrorMessages.UNSUPPORTED_EVENT_VERSION.formatted(envelope.eventVersion())
            );
        }

        switch (envelope.eventType()) {
            case KafkaEventConstants.USER_CREATED -> {
                UserCreatedEvent event = objectMapper.treeToValue(
                        envelope.payload(),
                        UserCreatedEvent.class
                );

                accountLinkService.linkAccount(
                        envelope.eventId(),
                        event.employeeId(),
                        event.userId()
                );
            }

            case KafkaEventConstants.USER_DELETED -> {
                UserDeletedEvent event = objectMapper.treeToValue(
                        envelope.payload(),
                        UserDeletedEvent.class
                );

                accountLinkService.unlinkAccount(
                        envelope.eventId(),
                        event.employeeId(),
                        event.userId()
                );
            }

            default -> throw new IllegalArgumentException(
                    ErrorMessages.UNSUPPORTED_EVENT_TYPE.formatted(envelope.eventType())
            );
        }

        log.info(
                "Processed user event: eventId={}, type={}",
                envelope.eventId(),
                envelope.eventType()
        );
    }
}
