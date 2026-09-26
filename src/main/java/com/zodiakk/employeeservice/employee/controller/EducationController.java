package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.EducationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EducationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EducationResponseDto;
import com.zodiakk.employeeservice.employee.service.EducationService;
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
@RequestMapping("/api/employees/{employeeId}/education")
@RequiredArgsConstructor
@Tag(name = "Employee Education", description = "Operations for managing employee education")
public class EducationController {

    private final EducationService educationService;

    @PostMapping
    @Operation(summary = "Add education", description = "Adds an education record to an employee")
    @ApiResponse(responseCode = "201", description = "Education successfully added",
            content = @Content(schema = @Schema(implementation = EducationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EducationResponseDto> add(
            @Parameter(description = "Employee identifier", required = true) @PathVariable UUID employeeId,
            @Valid @RequestBody EducationCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(educationService.add(employeeId, request));
    }

    @GetMapping("/{educationId}")
    @Operation(summary = "Get education by ID", description = "Returns a specific education record of an employee")
    @ApiResponse(responseCode = "200", description = "Education found",
            content = @Content(schema = @Schema(implementation = EducationResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee or education record not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EducationResponseDto> getById(@PathVariable UUID employeeId, @PathVariable UUID educationId) {
        return ResponseEntity.ok(educationService.getById(employeeId, educationId));
    }

    @GetMapping
    @Operation(summary = "Get employee education", description = "Returns all education records of an employee")
    @ApiResponse(responseCode = "200", description = "Education records successfully retrieved")
    public ResponseEntity<List<EducationResponseDto>> listByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(educationService.listByEmployee(employeeId));
    }

    @PutMapping("/{educationId}")
    @Operation(summary = "Update education", description = "Updates an employee education record")
    @ApiResponse(responseCode = "200", description = "Education successfully updated",
            content = @Content(schema = @Schema(implementation = EducationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee or education record not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EducationResponseDto> update(@PathVariable UUID employeeId, @PathVariable UUID educationId,
                                                       @Valid @RequestBody EducationUpdateRequestDto request) {
        return ResponseEntity.ok(educationService.update(employeeId, educationId, request));
    }

    @DeleteMapping("/{educationId}")
    @Operation(summary = "Delete education", description = "Deletes an employee education record")
    @ApiResponse(responseCode = "204", description = "Education successfully deleted")
    @ApiResponse(responseCode = "404", description = "Employee or education record not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID employeeId, @PathVariable UUID educationId) {
        educationService.delete(employeeId, educationId);
        return ResponseEntity.noContent().build();
    }
}