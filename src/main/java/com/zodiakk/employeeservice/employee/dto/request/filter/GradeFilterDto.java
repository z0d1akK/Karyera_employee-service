package com.zodiakk.employeeservice.employee.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeFilterDto {

    @Schema(description = "Part of grade name, case-insensitive", example = "senior")
    private String name;

    @Schema(description = "Exact grade level", example = "5")
    private Integer level;

    @Schema(description = "Grade activity flag", example = "true")
    private Boolean active;

    @Schema(description = "Part of grade description, case-insensitive", example = "experienced")
    private String description;
}
