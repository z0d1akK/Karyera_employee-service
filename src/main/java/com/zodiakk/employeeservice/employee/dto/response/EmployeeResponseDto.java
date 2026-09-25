package com.zodiakk.employeeservice.employee.dto.response;

import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDto {

    @Schema(description = "Employee identifier")
    private UUID id;

    @Schema(description = "User identifier from Auth Service")
    private UUID userId;

    @Schema(description = "Employee first name")
    private String firstName;

    @Schema(description = "Employee last name")
    private String lastName;

    @Schema(description = "Employee middle name")
    private String middleName;

    @Schema(description = "Employee birth date")
    private LocalDate birthDate;

    @Schema(description = "Employee hire date")
    private LocalDate hireDate;

    @Schema(description = "Employee position")
    private PositionResponseDto position;

    @Schema(description = "Employee specialization")
    private SpecializationResponseDto specialization;

    @Schema(description = "Employee grade")
    private GradeResponseDto grade;

    @Schema(description = "Employee department")
    private DepartmentResponseDto department;

    @Schema(description = "Employee maximum subordinates")
    private Integer maxSubordinates;

    @Schema(description = "Employee employment status")
    private EmploymentStatus employmentStatus;

    @Schema(description = "Optimistic lock version")
    private Long version;

    @Schema(description = "Employee profile photo")
    private EmployeeProfilePhotoResponseDto profilePhoto;

    @Schema(description = "Employee skills")
    private List<EmployeeSkillResponseDto> skills;

    @Schema(description = "Employee experiences")
    private List<ExperienceResponseDto> experiences;

    @Schema(description = "Employee educations")
    private List<EducationResponseDto> educations;

    @Schema(description = "Employee military obligation")
    private MilitaryObligationResponseDto militaryObligation;

    @Schema(description = "Employee responsibilities")
    private List<EmployeeResponsibilityResponseDto> responsibilities;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}