package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.DepartmentCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.DepartmentResponseDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface DepartmentMapper {

    Department toEntity(DepartmentCreateRequestDto dto);

    @Mapping(target = "id", source = "id")
    DepartmentResponseDto toResponseDto(Department department);

    void updateEntity(DepartmentUpdateRequestDto dto, @MappingTarget Department department);
}