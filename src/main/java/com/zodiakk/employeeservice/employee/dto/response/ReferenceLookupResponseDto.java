package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Batch catalog lookup response")
public record ReferenceLookupResponseDto(

        @Schema(description = "Resolved catalog entries")
        List<ReferenceLookupItemDto> items
) {
}
