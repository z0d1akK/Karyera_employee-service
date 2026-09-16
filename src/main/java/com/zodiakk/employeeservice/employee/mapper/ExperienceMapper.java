package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.ExperienceCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ExperienceUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ExperienceResponseDto;
import com.zodiakk.employeeservice.employee.entity.Experience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ExperienceMapper {

    @Mapping(target = "employee", ignore = true)
    Experience toEntity(ExperienceCreateRequestDto dto);

    @Mapping(target = "employeeId", source = "employee.id")
    ExperienceResponseDto toResponseDto(Experience experience);

    @Mapping(target = "employee", ignore = true)
    void updateEntity(ExperienceUpdateRequestDto dto, @MappingTarget Experience experience);
}