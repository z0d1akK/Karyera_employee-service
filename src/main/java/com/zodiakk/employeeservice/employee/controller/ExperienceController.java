package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.ExperienceCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ExperienceUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ExperienceResponseDto;
import com.zodiakk.employeeservice.employee.service.ExperienceService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees/{employeeId}/experience")
@RequiredArgsConstructor
@Tag(name = "Employee Experience", description = "Operations for managing employee experience")
public class ExperienceController {

    private final ExperienceService experienceService;

    @PostMapping
    @Operation(summary = "Add experience", description = "Adds an experience record to an employee")
    @ApiResponse(responseCode = "201", description = "Experience successfully added",
            content = @Content(schema = @Schema(implementation = ExperienceResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ExperienceResponseDto> add(@Parameter(description = "Employee identifier", required = true) @PathVariable UUID employeeId,
                                                     @Valid @RequestBody ExperienceCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(experienceService.add(employeeId, request));
    }

    @GetMapping("/{experienceId}")
    @Operation(summary = "Get experience by ID", description = "Returns a specific experience record of an employee")
    @ApiResponse(responseCode = "200", description = "Experience found",
            content = @Content(schema = @Schema(implementation = ExperienceResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee or experience record not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ExperienceResponseDto> getById(@PathVariable UUID employeeId, @PathVariable UUID experienceId) {
        return ResponseEntity.ok(experienceService.getById(employeeId, experienceId));
    }

    @GetMapping
    @Operation(summary = "Get employee experience", description = "Returns all experience records of an employee")
    @ApiResponse(responseCode = "200", description = "Experience records successfully retrieved")
    public ResponseEntity<List<ExperienceResponseDto>> listByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(experienceService.listByEmployee(employeeId));
    }

    @PutMapping("/{experienceId}")
    @Operation(summary = "Update experience", description = "Updates an employee experience record")
    @ApiResponse(responseCode = "200", description = "Experience successfully updated",
            content = @Content(schema = @Schema(implementation = ExperienceResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee or experience record not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ExperienceResponseDto> update(@PathVariable UUID employeeId, @PathVariable UUID experienceId,
                                                        @Valid @RequestBody ExperienceUpdateRequestDto request) {
        return ResponseEntity.ok(experienceService.update(employeeId, experienceId, request));
    }

    @DeleteMapping("/{experienceId}")
    @Operation(summary = "Delete experience", description = "Deletes an employee experience record")
    @ApiResponse(responseCode = "204", description = "Experience successfully deleted")
    @ApiResponse(responseCode = "404", description = "Employee or experience record not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID employeeId, @PathVariable UUID experienceId) {
        experienceService.delete(employeeId, experienceId);
        return ResponseEntity.noContent().build();
    }
}
