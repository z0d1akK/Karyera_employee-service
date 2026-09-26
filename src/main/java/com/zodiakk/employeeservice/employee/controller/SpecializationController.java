package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.SpecializationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SpecializationFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SpecializationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SpecializationResponseDto;
import com.zodiakk.employeeservice.employee.service.SpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees/specializations")
@RequiredArgsConstructor
@Tag(name = "Specializations", description = "Operations for managing employee specializations")
public class SpecializationController {

    private final SpecializationService specializationService;

    @PostMapping
    @Operation(summary = "Create specialization", description = "Creates a new employee specialization")
            @ApiResponse(responseCode = "201", description = "Specialization successfully created",
                    content = @Content(schema = @Schema(implementation = SpecializationResponseDto.class)))
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
            @ApiResponse(responseCode = "409", description = "Specialization with the specified name already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<SpecializationResponseDto> create(@Valid @RequestBody SpecializationCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specializationService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specialization by ID")
            @ApiResponse(responseCode = "200", description = "Specialization found",
                    content = @Content(schema = @Schema(implementation = SpecializationResponseDto.class)))
            @ApiResponse(responseCode = "404", description = "Specialization not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<SpecializationResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(specializationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get specializations", description = "Returns a paginated and filtered list of specializations")
    @ApiResponse(responseCode = "200", description = "Specializations successfully retrieved")
    public ResponseEntity<Page<SpecializationResponseDto>> findAll(@Valid SpecializationFilterDto filter, @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(specializationService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update specialization")
            @ApiResponse(responseCode = "200", description = "Specialization successfully updated",
                    content = @Content(schema = @Schema(implementation = SpecializationResponseDto.class)))
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
            @ApiResponse(responseCode = "404", description = "Specialization not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            @ApiResponse(responseCode = "409", description = "Specialization with the specified name already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<SpecializationResponseDto> update(@PathVariable UUID id, @Valid @RequestBody SpecializationUpdateRequestDto request) {
        return ResponseEntity.ok(specializationService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate specialization")
    @ApiResponse(responseCode = "204", description = "Specialization successfully activated")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        specializationService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate specialization")
    @ApiResponse(responseCode = "204", description = "Specialization successfully deactivated")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        specializationService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete specialization", description = "Deletes a specialization that is not assigned to employees")
            @ApiResponse(responseCode = "204", description = "Specialization successfully deleted")
            @ApiResponse(responseCode = "404", description = "Specialization not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            @ApiResponse(responseCode = "409", description = "Specialization is currently used by employees",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        specializationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}