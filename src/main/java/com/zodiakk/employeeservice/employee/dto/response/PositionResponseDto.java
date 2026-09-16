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
public class PositionResponseDto {

    @Schema(description = "Position identifier")
    private UUID id;

    @Schema(description = "Position name")
    private String name;

    @Schema(description = "Position description")
    private String description;

    @Schema(description = "Position activity flag")
    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
