package com.zodiakk.employeeservice.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Catalog reference data used by other services")
public record ReferenceLookupItemDto(

        @Schema(description = "Catalog identifier")
        UUID id,

        @Schema(description = "Catalog display name")
        String name,

        @Schema(description = "Whether the catalog entry is active")
        Boolean active
) {
}
