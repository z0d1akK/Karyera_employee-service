package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.ResponsibilityTypeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ResponsibilityTypeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ResponsibilityTypeResponseDto;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ResponsibilityTypeMapper {

    ResponsibilityType toEntity(ResponsibilityTypeCreateRequestDto dto);

    ResponsibilityTypeResponseDto toResponseDto(ResponsibilityType responsibilityType);

    void updateEntity(ResponsibilityTypeUpdateRequestDto dto, @MappingTarget ResponsibilityType responsibilityType);
}
