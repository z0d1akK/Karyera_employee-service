package com.zodiakk.employeeservice.integration.auth.service.impl;

import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.integration.auth.entity.EmployeeAccountUnlink;
import com.zodiakk.employeeservice.integration.auth.exception.EmployeeAccountLinkConflictException;
import com.zodiakk.employeeservice.integration.auth.repository.EmployeeAccountUnlinkRepository;
import com.zodiakk.employeeservice.integration.auth.repository.ProcessedEventRepository;
import com.zodiakk.employeeservice.integration.auth.service.EmployeeAccountEventService;
import com.zodiakk.employeeservice.integration.auth.service.EmployeeAccountLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeAccountLinkServiceImpl
        implements EmployeeAccountLinkService {

    private final EmployeeRepository employeeRepository;

    private final ProcessedEventRepository processedEventRepository;

    private final EmployeeAccountUnlinkRepository unlinkRepository;

    private final EmployeeAccountEventService eventService;

    @Override
    @Transactional
    public void linkAccount(
            UUID eventId,
            UUID employeeId,
            UUID userId
    ) {
        Employee employee = employeeRepository.findByIdForUpdate(employeeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Employee not found: " + employeeId
                ));

        int registered = processedEventRepository.registerIfAbsent(eventId);

        if (registered == 0) {
            return;
        }

        if (unlinkRepository.existsById(userId)) {
            log.info(
                    "Ignoring obsolete USER_CREATED: userId={}",
                    userId
            );
            return;
        }

        UUID existingUserId = employee.getUserId();

        if (existingUserId == null) {
            employee.setUserId(userId);
            employeeRepository.saveAndFlush(employee);
            return;
        }

        if (!existingUserId.equals(userId)) {
            throw new EmployeeAccountLinkConflictException(
                    employeeId,
                    existingUserId,
                    userId
            );
        }
    }

    @Override
    @Transactional
    public void unlinkAccount(
            UUID eventId,
            UUID employeeId,
            UUID userId
    ) {
        Employee employee = employeeRepository.findByIdForUpdate(employeeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Employee not found: " + employeeId
                ));

        int registered = processedEventRepository.registerIfAbsent(eventId);

        if (registered == 0) {
            return;
        }

        UUID existingUserId = employee.getUserId();

        if (existingUserId != null && !existingUserId.equals(userId)) {
            throw new EmployeeAccountLinkConflictException(
                    employeeId,
                    existingUserId,
                    userId
            );
        }

        if (existingUserId != null) {
            employee.setUserId(null);
            employeeRepository.saveAndFlush(employee);
        }

        unlinkRepository.save(
                EmployeeAccountUnlink.builder()
                        .userId(userId)
                        .employeeId(employeeId)
                        .unlinkedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );

        eventService.recordAccountUnlinked(userId, employeeId);
    }
}