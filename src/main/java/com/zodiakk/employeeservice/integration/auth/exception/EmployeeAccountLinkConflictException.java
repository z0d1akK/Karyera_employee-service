package com.zodiakk.employeeservice.integration.auth.exception;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;

import java.util.UUID;

public class EmployeeAccountLinkConflictException extends RuntimeException {

    public EmployeeAccountLinkConflictException(UUID employeeId, UUID existingUserId, UUID requestedUserId) {
        super(
                ErrorMessages.ACCOUNT_ALREADY_LINKED.formatted(
                        employeeId,
                        existingUserId,
                        requestedUserId
                )
        );
    }
}