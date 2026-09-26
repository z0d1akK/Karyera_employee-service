package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.ResponsibilityTypeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.ResponsibilityTypeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ResponsibilityTypeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ResponsibilityTypeResponseDto;
import com.zodiakk.employeeservice.employee.service.ResponsibilityTypeService;
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
@RequestMapping("/api/employees/responsibility-types")
@RequiredArgsConstructor
@Tag(name = "Responsibility Types", description = "Operations for managing employee responsibility types")
public class ResponsibilityTypeController {

    private final ResponsibilityTypeService responsibilityTypeService;

    @PostMapping
    @Operation(summary = "Create responsibility type", description = "Creates a new employee responsibility type")
    @ApiResponse(responseCode = "201", description = "Responsibility type successfully created",
            content = @Content(schema = @Schema(implementation = ResponsibilityTypeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Responsibility type with the specified code already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ResponsibilityTypeResponseDto> create(@Valid @RequestBody ResponsibilityTypeCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responsibilityTypeService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get responsibility type by ID")
    @ApiResponse(responseCode = "200", description = "Responsibility type found",
            content = @Content(schema = @Schema(implementation = ResponsibilityTypeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Responsibility type not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ResponsibilityTypeResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(responsibilityTypeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get responsibility types", description = "Returns a paginated and filtered list of responsibility types")
    @ApiResponse(responseCode = "200", description = "Responsibility types successfully retrieved")
    public ResponseEntity<Page<ResponsibilityTypeResponseDto>> findAll(@Valid ResponsibilityTypeFilterDto filter, @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(responsibilityTypeService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update responsibility type")
    @ApiResponse(responseCode = "200", description = "Responsibility type successfully updated",
            content = @Content(schema = @Schema(implementation = ResponsibilityTypeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Responsibility type not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Responsibility type with the specified code already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ResponsibilityTypeResponseDto> update(@PathVariable UUID id, @Valid @RequestBody ResponsibilityTypeUpdateRequestDto request) {
        return ResponseEntity.ok(responsibilityTypeService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate responsibility type")
    @ApiResponse(responseCode = "204", description = "Responsibility type successfully activated")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        responsibilityTypeService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate responsibility type")
    @ApiResponse(responseCode = "204", description = "Responsibility type successfully deactivated")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        responsibilityTypeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete responsibility type", description = "Deletes a responsibility type that is not used by employee responsibilities")
    @ApiResponse(responseCode = "204", description = "Responsibility type successfully deleted")
    @ApiResponse(responseCode = "404", description = "Responsibility type not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Responsibility type is currently used",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        responsibilityTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}