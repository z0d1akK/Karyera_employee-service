package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

    List<Experience> findAllByEmployeeIdOrderByStartDateDesc(UUID employeeId);
}
