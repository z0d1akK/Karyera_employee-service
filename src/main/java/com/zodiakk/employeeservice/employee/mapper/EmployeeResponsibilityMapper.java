package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeResponsibilityCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeResponsibilityUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponsibilityResponseDto;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class,
        uses = {EmployeeMapper.class, ResponsibilityTypeMapper.class}
)
public interface EmployeeResponsibilityMapper {

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "responsibleEmployee", ignore = true)
    @Mapping(target = "responsibilityType", ignore = true)
    EmployeeResponsibility toEntity(EmployeeResponsibilityCreateRequestDto dto);

    @Mapping(target = "employeeId", source = "employee.id")
    EmployeeResponsibilityResponseDto toResponseDto(EmployeeResponsibility responsibility);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "responsibleEmployee", ignore = true)
    @Mapping(target = "responsibilityType", ignore = true)
    void updateEntity(EmployeeResponsibilityUpdateRequestDto dto, @MappingTarget EmployeeResponsibility responsibility);
}