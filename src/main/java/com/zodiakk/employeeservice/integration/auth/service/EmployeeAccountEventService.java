package com.zodiakk.employeeservice.integration.auth.service;

import com.zodiakk.employeeservice.integration.auth.dto.EmployeeAccountUnlinkedEvent;
import com.zodiakk.employeeservice.outbox.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeAccountEventService {

    private final OutboxEventService outboxEventService;

    public void recordAccountUnlinked(UUID userId, UUID employeeId) {
        outboxEventService.saveEvent(
                employeeId,
                "EMPLOYEE_ACCOUNT",
                "EMPLOYEE_ACCOUNT_UNLINKED",
                1,
                new EmployeeAccountUnlinkedEvent(userId, employeeId)
        );
    }
}