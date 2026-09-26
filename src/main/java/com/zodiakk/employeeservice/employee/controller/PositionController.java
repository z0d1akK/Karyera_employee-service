package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.PositionCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.PositionFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.PositionUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.PositionResponseDto;
import com.zodiakk.employeeservice.employee.service.PositionService;
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
@RequestMapping("/api/employees/positions")
@RequiredArgsConstructor
@Tag(name = "Positions", description = "Operations for managing employee positions")
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @Operation(summary = "Create position", description = "Creates a new employee position")
    @ApiResponse(responseCode = "201", description = "Position successfully created",
            content = @Content(schema = @Schema(implementation = PositionResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Position with the specified name already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<PositionResponseDto> create(@Valid @RequestBody PositionCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get position by ID")
    @ApiResponse(responseCode = "200", description = "Position found",
            content = @Content(schema = @Schema(implementation = PositionResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Position not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<PositionResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(positionService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get positions", description = "Returns a paginated and filtered list of positions")
    @ApiResponse(responseCode = "200", description = "Positions successfully retrieved")
    public ResponseEntity<Page<PositionResponseDto>> findAll(@Valid PositionFilterDto filter, @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(positionService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update position")
    @ApiResponse(responseCode = "200", description = "Position successfully updated",
            content = @Content(schema = @Schema(implementation = PositionResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Position not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Position with the specified name already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<PositionResponseDto> update(@PathVariable UUID id, @Valid @RequestBody PositionUpdateRequestDto request) {
        return ResponseEntity.ok(positionService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate position")
    @ApiResponse(responseCode = "204", description = "Position successfully activated")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        positionService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate position")
    @ApiResponse(responseCode = "204", description = "Position successfully deactivated")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        positionService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete position", description = "Deletes a position that is not used by employees")
    @ApiResponse(responseCode = "204", description = "Position successfully deleted")
    @ApiResponse(responseCode = "404", description = "Position not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Position is currently used by employees",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}