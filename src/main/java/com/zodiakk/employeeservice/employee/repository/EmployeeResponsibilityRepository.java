package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeResponsibilityRepository extends JpaRepository<EmployeeResponsibility, UUID> {

    List<EmployeeResponsibility> findAllByEmployeeId(UUID employeeId);

    List<EmployeeResponsibility> findAllByResponsibleEmployeeId(UUID responsibleEmployeeId);

    List<EmployeeResponsibility> findAllByResponsibleEmployeeIdAndResponsibilityTypeCode
            (UUID responsibleEmployeeId, String responsibilityTypeCode);

    Optional<EmployeeResponsibility>
    findByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull
            (UUID employeeId, UUID responsibleEmployeeId, UUID responsibilityTypeId);

    long countByResponsibleEmployeeIdAndResponsibilityTypeCodeAndEndDateIsNull
            (UUID responsibleEmployeeId, String responsibilityTypeCode);

    boolean existsByEmployeeIdAndResponsibleEmployeeIdAndResponsibilityTypeIdAndEndDateIsNull
            (UUID employeeId, UUID responsibleEmployeeId, UUID responsibilityTypeId);
}
