package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentCreateRequestDto {

    @Schema(description = "Department name", example = "Backend Development")
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(min = 2, max = 150, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Schema(description = "Department description")
    @Size(max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
    private String description;
}
