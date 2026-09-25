package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeProfilePhotoCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeProfilePhotoUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import com.zodiakk.employeeservice.employee.entity.EmployeeProfilePhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface EmployeeProfilePhotoMapper {

    @Mapping(target = "employee", ignore = true)
    EmployeeProfilePhoto toEntity(EmployeeProfilePhotoCreateRequestDto dto);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "url", ignore = true)
    EmployeeProfilePhotoResponseDto toResponseDto(EmployeeProfilePhoto profilePhoto);

    @Mapping(target = "employee", ignore = true)
    void updateEntity(EmployeeProfilePhotoUpdateRequestDto dto, @MappingTarget EmployeeProfilePhoto profilePhoto);
}