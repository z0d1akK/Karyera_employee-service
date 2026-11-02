package com.zodiakk.employeeservice.integration.employee.service;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.integration.employee.dto.EmployeePositionChangedEvent;
import com.zodiakk.employeeservice.integration.employee.dto.EmployeeSkillUpdatedEvent;
import com.zodiakk.employeeservice.outbox.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeEventService {

    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.employee-events}")
    private String employeeEventsTopic;

    public void recordSkillUpdated(
            UUID employeeId,
            UUID skillId,
            Integer level,
            BigDecimal yearsOfExperience,
            LocalDate lastAssessedAt,
            String changeType
    ) {
        outboxEventService.saveEvent(
                employeeEventsTopic,
                employeeId,
                KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE,
                KafkaEventConstants.EMPLOYEE_SKILL_UPDATED,
                KafkaEventConstants.EVENT_VERSION_V1,
                new EmployeeSkillUpdatedEvent(
                        employeeId,
                        skillId,
                        level,
                        yearsOfExperience,
                        lastAssessedAt,
                        changeType
                )
        );
    }

    public void recordPositionChanged(
            UUID employeeId,
            UUID previousPositionId,
            UUID previousGradeId,
            UUID positionId,
            UUID gradeId
    ) {
        outboxEventService.saveEvent(
                employeeEventsTopic,
                employeeId,
                KafkaEventConstants.AGGREGATE_TYPE_EMPLOYEE,
                KafkaEventConstants.EMPLOYEE_POSITION_CHANGED,
                KafkaEventConstants.EVENT_VERSION_V1,
                new EmployeePositionChangedEvent(
                        employeeId,
                        previousPositionId,
                        previousGradeId,
                        positionId,
                        gradeId
                )
        );
    }
}
