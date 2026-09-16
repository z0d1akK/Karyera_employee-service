package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, UUID> {

    List<EmployeeSkill> findAllByEmployeeId(UUID employeeId);

    Optional<EmployeeSkill> findByEmployeeIdAndSkillId(UUID employeeId, UUID skillId);

    boolean existsByEmployeeIdAndSkillId(UUID employeeId, UUID skillId);

    void deleteByEmployeeIdAndSkillId(UUID employeeId, UUID skillId);
}
