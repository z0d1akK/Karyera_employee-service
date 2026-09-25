package com.zodiakk.employeeservice.employee.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillFilterDto {

    @Schema(description = "Part of skill name, case-insensitive", example = "java")
    private String name;

    @Schema(description = "Part of skill category, case-insensitive", example = "backend")
    private String category;

    @Schema(description = "Skill activity flag", example = "true")
    private Boolean active;

    @Schema(description = "Part of skill description, case-insensitive", example = "programming")
    private String description;
}
