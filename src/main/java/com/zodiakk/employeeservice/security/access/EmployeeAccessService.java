package com.zodiakk.employeeservice.security.access;

import com.zodiakk.employeeservice.common.constants.SecurityConstants;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("employeeAccess")
@RequiredArgsConstructor
public class EmployeeAccessService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeResponsibilityRepository employeeResponsibilityRepository;

    public boolean isPrivileged() {
        return SecurityUtils.hasAnyRole(
                SecurityConstants.HR_AUTHORITY,
                SecurityConstants.ADMIN_AUTHORITY
        );
    }

    public boolean isSelf(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getUserId)
                .map(SecurityUtils.getCurrentUserId()::equals)
                .orElse(false);
    }

    public boolean isSelfUser(UUID userId) {
        return SecurityUtils.getCurrentUserId().equals(userId);
    }

    public boolean isManagerOf(UUID employeeId) {
        return employeeRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .map(Employee::getId)
                .map(managerId -> employeeResponsibilityRepository
                        .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(
                                employeeId,
                                managerId
                        ))
                .orElse(false);
    }

    public boolean canViewEmployee(UUID employeeId) {
        return isPrivileged() || isSelf(employeeId) || isManagerOf(employeeId);
    }

    public boolean canViewUser(UUID userId) {
        if (isPrivileged() || isSelfUser(userId)) {
            return true;
        }

        return employeeRepository.findByUserId(userId)
                .map(Employee::getId)
                .map(this::isManagerOf)
                .orElse(false);
    }

    public boolean canEditEmployee(UUID employeeId) {
        return isPrivileged() || isSelf(employeeId);
    }

    public boolean canManageEmployeeProfiles() {
        return isPrivileged();
    }

    public boolean canManageCatalogs() {
        return SecurityUtils.hasRole(SecurityConstants.ADMIN_AUTHORITY);
    }

    public boolean canListEmployees() {
        return SecurityUtils.hasAnyRole(
                SecurityConstants.EMPLOYEE_AUTHORITY,
                SecurityConstants.MANAGER_AUTHORITY,
                SecurityConstants.HR_AUTHORITY,
                SecurityConstants.ADMIN_AUTHORITY
        );
    }

    public boolean isManagerOnly() {
        return SecurityUtils.hasRole(SecurityConstants.MANAGER_AUTHORITY);
    }
}
