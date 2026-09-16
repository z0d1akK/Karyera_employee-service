package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.PositionCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.PositionUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.PositionResponseDto;
import com.zodiakk.employeeservice.employee.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PositionMapper {

    Position toEntity(PositionCreateRequestDto dto);

    PositionResponseDto toResponseDto(Position position);

    void updateEntity(PositionUpdateRequestDto dto, @MappingTarget Position position);
}