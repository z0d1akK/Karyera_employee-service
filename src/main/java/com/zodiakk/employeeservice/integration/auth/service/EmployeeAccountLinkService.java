package com.zodiakk.employeeservice.integration.auth.service;

import java.util.UUID;

public interface EmployeeAccountLinkService {

    void linkAccount(UUID eventId, UUID employeeId, UUID userId);

    void unlinkAccount(UUID eventId, UUID employeeId, UUID userId);
}