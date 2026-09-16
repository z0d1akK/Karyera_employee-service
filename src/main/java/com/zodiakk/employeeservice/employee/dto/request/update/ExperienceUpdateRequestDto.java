package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceUpdateRequestDto {

    @NotBlank(message = ValidationMessages.COMPANY_REQUIRED)
    @Size(
            min = 2,
            max = 200,
            message = ValidationMessages.COMPANY_SIZE
    )
    private String company;

    @NotBlank(message = ValidationMessages.EXPERIENCE_POSITION_REQUIRED)
    @Size(
            min = 2,
            max = 150,
            message = ValidationMessages.EXPERIENCE_POSITION_SIZE
    )
    private String position;

    @Size(
            max = 1000,
            message = ValidationMessages.EXPERIENCE_DESCRIPTION_SIZE
    )
    private String description;

    @NotNull(message = ValidationMessages.START_DATE_REQUIRED)
    private LocalDate startDate;

    private LocalDate endDate;
}