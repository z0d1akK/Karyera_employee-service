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
public class GradeResponseDto {

    @Schema(description = "Grade identifier")
    private UUID id;

    @Schema(description = "Grade name")
    private String name;

    @Schema(description = "Grade level")
    private Integer level;

    @Schema(description = "Grade description")
    private String description;

    @Schema(description = "Grade activity flag")
    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}