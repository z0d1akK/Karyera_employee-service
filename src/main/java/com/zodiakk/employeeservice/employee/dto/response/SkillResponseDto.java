package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResponseDto {

    @Schema(description = "Skill identifier")
    private UUID id;

    @Schema(description = "Skill name")
    private String name;

    @Schema(description = "Skill category")
    private String category;

    @Schema(description = "Skill description")
    private String description;

    @Schema(description = "Skill activity flag")
    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}