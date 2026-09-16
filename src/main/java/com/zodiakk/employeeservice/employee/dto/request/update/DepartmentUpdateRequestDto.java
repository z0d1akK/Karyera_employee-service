package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentUpdateRequestDto {

    @Schema(description = "Department name", example = "Backend Development")
    @Size(min = 2, max = 150, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Schema(description = "Department description")
    @Size(max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
    private String description;

    @Schema(description = "Whether the department is active")
    private Boolean active;
}