package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResponsibilityTypeRepository extends JpaRepository<ResponsibilityType, UUID>, JpaSpecificationExecutor<ResponsibilityType> {

    Optional<ResponsibilityType> findByCode(String code);

    boolean existsByCode(String code);
}