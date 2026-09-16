package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.GradeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.GradeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.GradeResponseDto;
import com.zodiakk.employeeservice.employee.entity.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface GradeMapper {

    Grade toEntity(GradeCreateRequestDto dto);

    GradeResponseDto toResponseDto(Grade grade);

    void updateEntity(GradeUpdateRequestDto dto, @MappingTarget Grade grade);
}