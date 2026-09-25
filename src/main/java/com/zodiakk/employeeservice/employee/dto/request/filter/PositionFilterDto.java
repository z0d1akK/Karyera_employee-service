package com.zodiakk.employeeservice.employee.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionFilterDto {

    @Schema(description = "Part of position name, case-insensitive", example = "Java developer")
    private String name;

    @Schema(description = "Position activity flag", example = "true")
    private Boolean active;

    @Schema(description = "Part of position description, case-insensitive", example = "development")
    private String description;
}