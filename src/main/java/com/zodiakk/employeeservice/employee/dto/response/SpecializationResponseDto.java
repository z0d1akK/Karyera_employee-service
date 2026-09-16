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
public class SpecializationResponseDto {

    @Schema(description = "Specialization identifier")
    private UUID id;

    @Schema(description = "Specialization name")
    private String name;

    @Schema(description = "Specialization description")
    private String description;

    @Schema(description = "Specialization activity flag")
    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}