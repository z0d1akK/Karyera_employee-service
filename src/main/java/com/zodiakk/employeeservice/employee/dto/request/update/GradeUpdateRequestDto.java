package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeUpdateRequestDto {

    @Size(min = 2, max = 100, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Min(value = 1, message = ValidationMessages.GRADE_LEVEL_MIN)
    @Max(value = 100, message = ValidationMessages.GRADE_LEVEL_MAX)
    private Integer level;

    @Size(max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = ValidationMessages.ACTIVE_REQUIRED)
    private Boolean active;
}