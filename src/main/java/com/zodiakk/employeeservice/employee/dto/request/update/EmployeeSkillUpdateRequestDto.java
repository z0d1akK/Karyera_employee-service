package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkillUpdateRequestDto {

    @Min(value = 1, message = ValidationMessages.SKILL_LEVEL_RANGE)
    @Max(value = 5, message = ValidationMessages.SKILL_LEVEL_RANGE)
    private Integer level;

    @DecimalMin(value = "0.0", message = ValidationMessages.YEARS_OF_EXPERIENCE_MIN)
    @DecimalMax(value = "100.0", message = ValidationMessages.YEARS_OF_EXPERIENCE_MAX)
    private BigDecimal yearsOfExperience;
}
