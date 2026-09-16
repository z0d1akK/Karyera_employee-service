package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.SkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SkillResponseDto;
import com.zodiakk.employeeservice.employee.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface SkillMapper {

    Skill toEntity(SkillCreateRequestDto dto);

    SkillResponseDto toResponseDto(Skill skill);

    void updateEntity(SkillUpdateRequestDto dto, @MappingTarget Skill skill);
}