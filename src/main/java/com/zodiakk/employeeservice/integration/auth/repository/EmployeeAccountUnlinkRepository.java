package com.zodiakk.employeeservice.integration.auth.repository;

import com.zodiakk.employeeservice.integration.auth.entity.EmployeeAccountUnlink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeAccountUnlinkRepository extends JpaRepository<EmployeeAccountUnlink, UUID> {
}