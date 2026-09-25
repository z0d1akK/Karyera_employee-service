package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceCreateRequestDto {

    @NotBlank(message = ValidationMessages.COMPANY_REQUIRED)
    @Size(min = 2, max = 200, message = ValidationMessages.COMPANY_SIZE)
    private String company;

    @NotBlank(message = ValidationMessages.EXPERIENCE_POSITION_REQUIRED)
    @Size(min = 2, max = 150, message = ValidationMessages.EXPERIENCE_POSITION_SIZE)
    private String position;

    @Size(max = 1000, message = ValidationMessages.EXPERIENCE_DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = ValidationMessages.START_DATE_REQUIRED)
    @PastOrPresent(message = ValidationMessages.START_DATE_VALID)
    private LocalDate startDate;

    private LocalDate endDate;
}
