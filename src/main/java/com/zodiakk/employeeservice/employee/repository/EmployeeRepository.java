package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e
        FROM Employee e
        WHERE e.id = :employeeId
        """)
    Optional<Employee> findByIdForUpdate(@Param("employeeId") UUID employeeId);

    @Query("""
            SELECT e.id
            FROM Employee e
            WHERE e.employmentStatus = :status
            ORDER BY e.id
            """)
    List<UUID> findIdsByEmploymentStatus(@Param("status") EmploymentStatus status);

    @Query("""
            SELECT e.id
            FROM Employee e
            WHERE (:activeOnly = false OR e.employmentStatus = com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus.ACTIVE)
              AND (:positionId IS NULL OR e.position.id = :positionId)
              AND (:gradeId IS NULL OR e.grade.id = :gradeId)
              AND (:departmentId IS NULL OR e.department.id = :departmentId)
            ORDER BY e.id
            """)
    List<UUID> filterIds(
            @Param("activeOnly") boolean activeOnly,
            @Param("positionId") UUID positionId,
            @Param("gradeId") UUID gradeId,
            @Param("departmentId") UUID departmentId
    );
}
