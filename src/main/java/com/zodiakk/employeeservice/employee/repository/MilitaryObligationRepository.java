package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.MilitaryObligation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MilitaryObligationRepository extends JpaRepository<MilitaryObligation, UUID> {

    Optional<MilitaryObligation> findByEmployeeId(UUID employeeId);

    boolean existsByEmployeeId(UUID employeeId);
}
