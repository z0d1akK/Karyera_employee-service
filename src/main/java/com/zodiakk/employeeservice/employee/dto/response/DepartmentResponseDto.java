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
public class DepartmentResponseDto {

    @Schema(description = "Department identifier")
    private UUID id;

    @Schema(description = "Department name")
    private String name;

    @Schema(description = "Department description")
    private String description;

    @Schema(description = "Department activity flag")
    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
