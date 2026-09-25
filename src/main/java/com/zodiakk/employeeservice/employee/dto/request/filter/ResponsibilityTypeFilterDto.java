package com.zodiakk.employeeservice.employee.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsibilityTypeFilterDto {

    @Schema(description = "Part of responsibility type code, case-insensitive", example = "MENTOR")
    private String code;

    @Schema(description = "Part of responsibility type name, case-insensitive", example = "mentor")
    private String name;

    @Schema(description = "Responsibility type activity flag", example = "true")
    private Boolean active;

    @Schema(description = "Part of responsibility type description, case-insensitive", example = "guidance")
    private String description;

    @Schema(description = "Whether multiple assignments of this type are allowed", example = "false")
    private Boolean multipleAllowed;
}
