package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Career profiles resolved for the requested employees")
public record CareerProfileLookupResponseDto(
        List<CareerProfileResponseDto> profiles
) {
}
