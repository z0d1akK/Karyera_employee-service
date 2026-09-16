package com.zodiakk.employeeservice.employee.repository;

import com.zodiakk.employeeservice.employee.entity.EmployeeProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfilePhotoRepository extends JpaRepository<EmployeeProfilePhoto, UUID> {

    Optional<EmployeeProfilePhoto> findByEmployeeId(UUID employeeId);

    boolean existsByEmployeeId(UUID employeeId);

    Optional<EmployeeProfilePhoto> findByObjectKey(String objectKey);
}
