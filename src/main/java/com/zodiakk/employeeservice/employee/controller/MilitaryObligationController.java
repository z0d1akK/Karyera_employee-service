package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.MilitaryObligationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.MilitaryObligationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.MilitaryObligationResponseDto;
import com.zodiakk.employeeservice.employee.service.MilitaryObligationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees/{employeeId}/military-obligation")
@RequiredArgsConstructor
@Tag(name = "Military Obligation", description = "Operations for managing employee military obligation")
public class MilitaryObligationController {

    private final MilitaryObligationService militaryObligationService;

    @PostMapping
    @Operation(summary = "Create military obligation", description = "Creates a military obligation for an employee")
    @ApiResponse(responseCode = "201", description = "Military obligation successfully created",
            content = @Content(schema = @Schema(implementation = MilitaryObligationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Military obligation already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<MilitaryObligationResponseDto> create(
            @Parameter(description = "Employee identifier", required = true) @PathVariable UUID employeeId,
            @Valid @RequestBody MilitaryObligationCreateRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(militaryObligationService.create(employeeId, request));
    }

    @GetMapping
    @Operation(summary = "Get military obligation", description = "Returns the military obligation of an employee")
    @ApiResponse(responseCode = "200", description = "Military obligation successfully retrieved",
            content = @Content(schema = @Schema(implementation = MilitaryObligationResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee or military obligation not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<MilitaryObligationResponseDto> getByEmployeeId(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(militaryObligationService.getByEmployeeId(employeeId));
    }

    @PutMapping
    @Operation(summary = "Upsert military obligation", description = "Creates or replaces the military obligation "
                    + "of an employee"
    )
    @ApiResponse(responseCode = "200", description = "Military obligation successfully saved",
            content = @Content(schema = @Schema(implementation = MilitaryObligationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<MilitaryObligationResponseDto> upsert(
            @PathVariable UUID employeeId,
            @Valid @RequestBody MilitaryObligationCreateRequestDto request
    ) {
        return ResponseEntity.ok(militaryObligationService.upsert(employeeId, request));
    }

    @PatchMapping
    @Operation(summary = "Update military obligation", description = "Partially updates the military obligation "
                    + "of an employee"
    )
    @ApiResponse(responseCode = "200", description = "Military obligation successfully updated",
            content = @Content(schema = @Schema(implementation = MilitaryObligationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee or military obligation not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<MilitaryObligationResponseDto> update(
            @PathVariable UUID employeeId,
            @Valid @RequestBody MilitaryObligationUpdateRequestDto request
    ) {
        return ResponseEntity.ok(militaryObligationService.update(employeeId, request));
    }

    @DeleteMapping
    @Operation(summary = "Delete military obligation", description = "Deletes the military obligation of an employee")
    @ApiResponse(responseCode = "204", description = "Military obligation successfully deleted")
    @ApiResponse(responseCode = "404", description = "Employee or military obligation not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID employeeId) {
        militaryObligationService.delete(employeeId);
        return ResponseEntity.noContent().build();
    }
}
