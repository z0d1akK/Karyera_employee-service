package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeSkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeSkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSkillResponseDto;
import com.zodiakk.employeeservice.employee.entity.EmployeeSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface EmployeeSkillMapper {

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "lastAssessedAt", ignore = true)
    EmployeeSkill toEntity(EmployeeSkillCreateRequestDto dto);

    @Mapping(target = "employeeId", source = "employee.id")
    EmployeeSkillResponseDto toResponseDto(EmployeeSkill employeeSkill);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "lastAssessedAt", ignore = true)
    void updateEntity(EmployeeSkillUpdateRequestDto dto, @MappingTarget EmployeeSkill employeeSkill);
}