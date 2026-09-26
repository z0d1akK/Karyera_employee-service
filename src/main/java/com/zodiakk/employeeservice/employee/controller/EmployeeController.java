package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.EmployeeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSummaryResponseDto;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Operations for managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create employee", description = "Creates a new employee")
    @ApiResponse(responseCode = "201", description = "Employee successfully created",
            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Employee with the specified data already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Returns a full employee profile by its identifier")
    @ApiResponse(responseCode = "200", description = "Employee found",
            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponseDto> getById(@Parameter(description = "Employee identifier", required = true)
                                                       @PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get employee by user ID", description = "Returns an employee profile associated with the specified user")
    @ApiResponse(responseCode = "200", description = "Employee found",
            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponseDto> getByUserId(@Parameter(description = "User identifier", required = true) @PathVariable UUID userId) {
        return ResponseEntity.ok(employeeService.getByUserId(userId));
    }

    @GetMapping
    @Operation(summary = "Get employees", description = "Returns a paginated and filtered list of employees")
    @ApiResponse(responseCode = "200", description = "Employees successfully retrieved")
    public ResponseEntity<Page<EmployeeSummaryResponseDto>> findAll(@Valid EmployeeFilterDto filter, @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(employeeService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee")
    @ApiResponse(responseCode = "200", description = "Employee successfully updated",
            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Employee cannot be updated with the specified data",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponseDto> update(
            @Parameter(description = "Employee identifier", required = true) @PathVariable UUID id,
            @Valid @RequestBody EmployeeUpdateRequestDto request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change employee status", description = "Changes the employment status of an employee")
    @ApiResponse(responseCode = "200", description = "Employee status successfully changed",
            content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponseDto> changeStatus(
            @Parameter(description = "Employee identifier", required = true) @PathVariable UUID id,
            @Parameter(description = "New employment status", required = true) @RequestParam EmploymentStatus status) {
        return ResponseEntity.ok(employeeService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Deletes an employee")
    @ApiResponse(responseCode = "204", description = "Employee successfully deleted")
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@Parameter(description = "Employee identifier", required = true) @PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}