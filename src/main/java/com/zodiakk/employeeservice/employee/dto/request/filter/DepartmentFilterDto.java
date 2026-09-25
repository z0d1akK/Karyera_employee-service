package com.zodiakk.employeeservice.employee.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentFilterDto {

    @Schema(description = "Part of department name, case-insensitive", example = "backend")
    private String name;

    @Schema(description = "Department activity flag", example = "true")
    private Boolean active;

    @Schema(description = "Part of department description, case-insensitive", example = "development")
    private String description;
}