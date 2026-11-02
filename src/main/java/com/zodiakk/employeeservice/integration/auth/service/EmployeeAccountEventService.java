package com.zodiakk.employeeservice.integration.auth.service;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.integration.auth.dto.EmployeeAccountUnlinkedEvent;
import com.zodiakk.employeeservice.outbox.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeAccountEventService {

    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.employee-account-events}")
    private String employeeAccountEventsTopic;

    public void recordAccountUnlinked(UUID userId, UUID employeeId) {
        outboxEventService.saveEvent(
                employeeAccountEventsTopic,
                employeeId,
                KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE_ACCOUNT,
                KafkaEventConstants.EMPLOYEE_ACCOUNT_UNLINKED,
                KafkaEventConstants.EVENT_VERSION_V1,
                new EmployeeAccountUnlinkedEvent(userId, employeeId)
        );
    }
}
