package com.zodiakk.employeeservice.integration.auth.exception;

import java.util.UUID;

public class EmployeeAccountLinkConflictException extends RuntimeException {

    public EmployeeAccountLinkConflictException(
            UUID employeeId,
            UUID existingUserId,
            UUID requestedUserId
    ) {
        super(
                "Employee %s is already linked to user %s; requested user %s"
                        .formatted(
                                employeeId,
                                existingUserId,
                                requestedUserId
                        )
        );
    }
}