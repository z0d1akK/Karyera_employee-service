package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import com.zodiakk.employeeservice.employee.entity.enums.EducationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class EducationCreateRequestDto {

    @Schema(description = "Education level", example = "SECONDARY")
    @NotNull(message = ValidationMessages.EDUCATION_LEVEL_REQUIRED)
    private EducationLevel educationLevel;

    @Schema(description = "Institution name", example = "BSUIR")
    @NotBlank(message = ValidationMessages.INSTITUTION_REQUIRED)
    @Size(min = 2, max = 250, message = ValidationMessages.INSTITUTION_SIZE)
    private String institutionName;

    @Schema(description = "Field of study", example = "CS")
    @Size(max = 200, message = ValidationMessages.FIELD_OF_STUDY_SIZE)
    private String fieldOfStudy;

    @Schema(description = "Degree name", example = "Software engineer")
    @Size(max = 200, message = ValidationMessages.DEGREE_NAME_SIZE)
    private String degreeName;

    @Schema(description = "Education start date", example = "2013-08-01")
    private LocalDate startDate;

    @Schema(description = "Education end date", example = "2024-08-01")
    private LocalDate endDate;
}