package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.EducationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EducationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EducationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Education;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface EducationMapper {

    @Mapping(target = "employee", ignore = true)
    Education toEntity(EducationCreateRequestDto dto);

    @Mapping(target = "employeeId", source = "employee.id")
    EducationResponseDto toResponseDto(Education education);

    @Mapping(target = "employee", ignore = true)
    void updateEntity(EducationUpdateRequestDto dto, @MappingTarget Education education);
}