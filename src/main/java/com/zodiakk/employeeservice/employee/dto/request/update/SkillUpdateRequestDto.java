package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillUpdateRequestDto {

    @Size(min = 2, max = 150, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Size(max = 100, message = ValidationMessages.CATEGORY_SIZE)
    private String category;

    @Size(max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = ValidationMessages.ACTIVE_REQUIRED)
    private Boolean active;
}