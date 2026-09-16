package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, UUID>, JpaSpecificationExecutor<Specialization> {

    boolean existsByNameIgnoreCase(String name);
}
