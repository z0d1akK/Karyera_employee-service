package com.zodiakk.employeeservice.employee.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecializationFilterDto {

    @Schema(description = "Part of specialization name, case-insensitive", example = "backend developer")
    private String name;

    @Schema(description = "Specialization activity flag", example = "true")
    private Boolean active;

    @Schema(description = "Part of specialization description, case-insensitive", example = "development")
    private String description;
}