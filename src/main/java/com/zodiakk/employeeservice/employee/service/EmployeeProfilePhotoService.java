package com.zodiakk.employeeservice.employee.service;

import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface EmployeeProfilePhotoService {

    EmployeeProfilePhotoResponseDto upload(UUID employeeId, MultipartFile file);

    EmployeeProfilePhotoResponseDto getByEmployeeId(UUID employeeId);

    void delete(UUID employeeId);
}
