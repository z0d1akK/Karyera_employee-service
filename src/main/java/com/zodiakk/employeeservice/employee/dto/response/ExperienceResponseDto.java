package com.zodiakk.employeeservice.employee.dto.response;

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
public class ExperienceResponseDto {

    @Schema(description = "Experience identifier")
    private UUID id;

    @Schema(description = " Experience employee identifier")
    private UUID employeeId;

    @Schema(description = "Experience company")
    private String company;

    @Schema(description = "Experience position")
    private String position;

    @Schema(description = "Experience description")
    private String description;

    @Schema(description = "Experience start date")
    private LocalDate startDate;

    @Schema(description = "Experience end date")
    private LocalDate endDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
