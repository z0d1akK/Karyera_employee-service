package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeResponsibilityRepository extends JpaRepository<EmployeeResponsibility, UUID> {

    List<EmployeeResponsibility> findAllByEmployeeId(UUID employeeId);

    List<EmployeeResponsibility> findAllByResponsibleEmployeeId(UUID responsibleEmployeeId);

    Optional<EmployeeResponsibility>
    findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull
            (UUID employeeId, UUID responsibleEmployeeId, UUID responsibilityTypeId);

    long countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull
            (UUID responsibleEmployeeId, String responsibilityTypeCode);

    long countByResponsibleEmployeeIdAndEndDateIsNull(UUID responsibleEmployeeId);

    boolean existsByResponsibilityTypeId(UUID responsibilityTypeId);

    @Query("""
            SELECT r FROM EmployeeResponsibility r
            WHERE r.endDate IS NULL
              AND (r.employee.id = :employeeId OR r.responsibleEmployee.id = :employeeId)
            """)
    List<EmployeeResponsibility> findActiveInvolvingEmployee(@Param("employeeId") UUID employeeId);

    void deleteByEmployeeIdOrResponsibleEmployeeId(UUID employeeId, UUID responsibleEmployeeId);
}
