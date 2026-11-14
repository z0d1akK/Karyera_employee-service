package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Professional profile used by Analytics Service")
public record CareerProfileResponseDto(
        UUID employeeId,
        UUID positionId,
        String positionName,
        UUID gradeId,
        String gradeName,
        boolean active,
        long dataVersion,
        List<SkillLevel> skills,
        List<ExperiencePeriod> experiencePeriods,
        String fullName
) {

    @Schema(description = "Confirmed skill level")
    public record SkillLevel(
            UUID skillId,
            String skillName,
            int level
    ) {
    }

    @Schema(description = "Professional experience interval")
    public record ExperiencePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
