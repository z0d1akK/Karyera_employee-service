package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.GradeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.GradeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.GradeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.GradeResponseDto;
import com.zodiakk.employeeservice.employee.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/employees/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Operations for managing employee grades")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @Operation(summary = "Create grade", description = "Creates a new employee grade")
    @ApiResponse(responseCode = "201", description = "Grade successfully created",
            content = @Content(schema = @Schema(implementation = GradeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Grade with the specified name or level already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<GradeResponseDto> create(@Valid @RequestBody GradeCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grade by ID", description = "Returns a grade by its identifier")
    @ApiResponse(responseCode = "200", description = "Grade found",
            content = @Content(schema = @Schema(implementation = GradeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Grade not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<GradeResponseDto> getById(
            @Parameter(description = "Grade identifier", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(gradeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get grades", description = "Returns a paginated and filtered list of grades")
    @ApiResponse(responseCode = "200", description = "Grades successfully retrieved")
    public ResponseEntity<Page<GradeResponseDto>> findAll(@Valid GradeFilterDto filter, @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(gradeService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update grade", description = "Updates an existing employee grade")
    @ApiResponse(responseCode = "200", description = "Grade successfully updated",
            content = @Content(schema = @Schema(implementation = GradeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Grade not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Grade with the specified name or level already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<GradeResponseDto> update(@PathVariable UUID id, @Valid @RequestBody GradeUpdateRequestDto request) {
        return ResponseEntity.ok(gradeService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate grade", description = "Activates an existing grade")
    @ApiResponse(responseCode = "204", description = "Grade successfully activated")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        gradeService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate grade", description = "Deactivates an existing grade")
    @ApiResponse(responseCode = "204", description = "Grade successfully deactivated")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        gradeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete grade", description = "Deletes a grade that is not used by employees")
    @ApiResponse(responseCode = "204", description = "Grade successfully deleted")
    @ApiResponse(responseCode = "404", description = "Grade not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Grade is currently used by employees",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}