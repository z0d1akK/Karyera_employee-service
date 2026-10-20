package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import com.zodiakk.employeeservice.employee.entity.EmployeeProfilePhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface EmployeeProfilePhotoMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "url", ignore = true)
    EmployeeProfilePhotoResponseDto toResponseDto(EmployeeProfilePhoto profilePhoto);
}