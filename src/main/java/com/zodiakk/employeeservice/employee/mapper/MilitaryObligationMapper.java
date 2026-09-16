package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.MilitaryObligationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.MilitaryObligationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.MilitaryObligationResponseDto;
import com.zodiakk.employeeservice.employee.entity.MilitaryObligation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface MilitaryObligationMapper {

    @Mapping(target = "employee", ignore = true)
    MilitaryObligation toEntity(MilitaryObligationCreateRequestDto dto);

    @Mapping(target = "employeeId", source = "employee.id")
    MilitaryObligationResponseDto toResponseDto(MilitaryObligation militaryObligation);

    @Mapping(target = "employee", ignore = true)
    void updateEntity(MilitaryObligationUpdateRequestDto dto, @MappingTarget MilitaryObligation militaryObligation);
}