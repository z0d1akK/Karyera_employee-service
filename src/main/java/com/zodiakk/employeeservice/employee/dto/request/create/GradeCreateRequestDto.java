package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeCreateRequestDto {

    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(min = 2, max = 100, message = ValidationMessages.NAME_SIZE)
    private String name;

    @NotNull(message = ValidationMessages.FIELD_REQUIRED)
    @Min(value = 1, message = ValidationMessages.GRADE_LEVEL_MIN)
    @Max(value = 100, message = ValidationMessages.GRADE_LEVEL_MAX)
    private Integer level;

    @Size(max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
    private String description;
}