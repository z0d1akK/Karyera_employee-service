package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    long countByPositionId(UUID positionId);

    long countByDepartmentId(UUID departmentId);

    long countByGradeId(UUID gradeId);

    long countBySpecializationId(UUID specializationId);
}
