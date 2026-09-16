package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfilePhotoResponseDto {

    @Schema(description = "Employee profile photo identifier")
    private UUID id;

    @Schema(description = "Employee profile photo employee identifier")
    private UUID employeeId;

    @Schema(description = "Employee profile photo object key")
    private String objectKey;

    @Schema(description = "Employee profile photo filename")
    private String fileName;

    @Schema(description = "Employee profile photo content type")
    private String contentType;

    @Schema(description = "Employee profile photo file size")
    private Long fileSize;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}