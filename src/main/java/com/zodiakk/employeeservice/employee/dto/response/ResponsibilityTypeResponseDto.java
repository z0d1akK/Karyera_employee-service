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
public class ResponsibilityTypeResponseDto {

    @Schema(description = "Responsibility type identifier")
    private UUID id;

    @Schema(description = "Responsibility type code")
    private String code;

    @Schema(description = "Responsibility type name")
    private String name;

    @Schema(description = "Responsibility type description")
    private String description;

    @Schema(description = "Responsibility type multiple allowed flag")
    private Boolean multipleAllowed;

    @Schema(description = "Responsibility type activity flag")
    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}