package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.SpecializationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SpecializationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SpecializationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface SpecializationMapper {

    Specialization toEntity(SpecializationCreateRequestDto dto);

    SpecializationResponseDto toResponseDto(Specialization specialization);

    void updateEntity(SpecializationUpdateRequestDto dto, @MappingTarget Specialization specialization);
}
