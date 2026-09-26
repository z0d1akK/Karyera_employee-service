package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.DepartmentCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.DepartmentFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.DepartmentResponseDto;
import com.zodiakk.employeeservice.employee.service.DepartmentService;
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
@RequestMapping("/api/employees/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Operations for managing employee departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "Create department", description = "Creates a new department")
    @ApiResponse(responseCode = "201", description = "Department successfully created",
            content = @Content(schema = @Schema(implementation = DepartmentResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Department with the specified name already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<DepartmentResponseDto> create(@Valid @RequestBody DepartmentCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Returns a department by its identifier")
    @ApiResponse(responseCode = "200", description = "Department found",
            content = @Content(schema = @Schema(implementation = DepartmentResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Department not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<DepartmentResponseDto> getById(
            @Parameter(description = "Department identifier", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get departments", description = "Returns a paginated and filtered list of departments")
    @ApiResponse(responseCode = "200", description = "Departments successfully retrieved")
    public ResponseEntity<Page<DepartmentResponseDto>> findAll(@Valid DepartmentFilterDto filter,
                                                               @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(departmentService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Updates an existing department")
    @ApiResponse(responseCode = "200", description = "Department successfully updated",
            content = @Content(schema = @Schema(implementation = DepartmentResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Department not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Department with the specified name already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<DepartmentResponseDto> update(
            @Parameter(description = "Department identifier", required = true) @PathVariable UUID id,
            @Valid @RequestBody DepartmentUpdateRequestDto request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate department", description = "Activates an existing department")
    @ApiResponse(responseCode = "204", description = "Department successfully activated")
    @ApiResponse(responseCode = "404", description = "Department not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        departmentService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate department", description = "Deactivates an existing department")
    @ApiResponse(responseCode = "204", description = "Department successfully deactivated")
    @ApiResponse(responseCode = "404", description = "Department not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        departmentService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Deletes a department that is not assigned to any employee")
    @ApiResponse(responseCode = "204", description = "Department successfully deleted")
    @ApiResponse(responseCode = "404", description = "Department not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Department is currently used by employees",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}