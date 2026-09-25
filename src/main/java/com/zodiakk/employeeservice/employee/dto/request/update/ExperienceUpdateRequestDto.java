package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceUpdateRequestDto {

    @Size(min = 2, max = 200, message = ValidationMessages.COMPANY_SIZE)
    private String company;

    @Size(min = 2, max = 150, message = ValidationMessages.EXPERIENCE_POSITION_SIZE)
    private String position;

    @Size(max = 1000, message = ValidationMessages.EXPERIENCE_DESCRIPTION_SIZE)
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;
}
