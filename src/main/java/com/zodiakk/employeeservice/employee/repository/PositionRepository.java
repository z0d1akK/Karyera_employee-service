package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID>, JpaSpecificationExecutor<Position> {

    boolean existsByNameIgnoreCase(String name);
}
