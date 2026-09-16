package com.zodiakk.employeeservice.employee.dto.response;

import com.zodiakk.employeeservice.employee.entity.enums.EducationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationResponseDto {

    @Schema(description = "Education identifier")
    private UUID id;

    @Schema(description = "Education employee identifier")
    private UUID employeeId;

    @Schema(description = "Education level")
    private EducationLevel educationLevel;

    @Schema(description = "Education institution name")
    private String institutionName;

    @Schema(description = "Education field of study")
    private String fieldOfStudy;

    @Schema(description = "Education degree name")
    private String degreeName;

    @Schema(description = "Education start date")
    private LocalDate startDate;

    @Schema(description = "Education end date")
    private LocalDate endDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}