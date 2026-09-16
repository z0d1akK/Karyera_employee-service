package com.zodiakk.employeeservice.employee.dto.response;

import com.zodiakk.employeeservice.employee.entity.enums.MilitaryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilitaryObligationResponseDto {

    @Schema(description = "Military obligation identifier")
    private UUID id;

    @Schema(description = "Military obligation employee identifier")
    private UUID employeeId;

    @Schema(description = "Military obligation status")
    private MilitaryStatus status;

    @Schema(description = "Military obligation rank")
    private String militaryRank;

    @Schema(description = "Military obligation category")
    private String category;

    @Schema(description = "Military obligation document number")
    private String documentNumber;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}