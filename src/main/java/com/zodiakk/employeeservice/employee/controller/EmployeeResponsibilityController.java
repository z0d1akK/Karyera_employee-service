package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeResponsibilityCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeResponsibilityUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponsibilityResponseDto;
import com.zodiakk.employeeservice.employee.service.EmployeeResponsibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Responsibilities", description = "Operations for managing employee responsibilities")
public class EmployeeResponsibilityController {

    private final EmployeeResponsibilityService employeeResponsibilityService;

    @PostMapping("/{employeeId}/responsibilities")
    @Operation(summary = "Assign responsibility", description = "Assigns a responsibility to an employee")
    @ApiResponse(responseCode = "201", description = "Responsibility successfully assigned",
            content = @Content(schema = @Schema(implementation = EmployeeResponsibilityResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee or responsibility type not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Responsibility cannot be assigned",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponsibilityResponseDto> assign(@Parameter(description = "Employee identifier", required = true)
                                                                    @PathVariable UUID employeeId,
                                                                    @Valid @RequestBody EmployeeResponsibilityCreateRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeResponsibilityService.assign(employeeId, request));
    }

    @GetMapping("/{employeeId}/responsibilities")
    @Operation(summary = "Get employee responsibilities", description = "Returns all responsibilities assigned to an employee")
    @ApiResponse(responseCode = "200", description = "Responsibilities successfully retrieved")
    public ResponseEntity<List<EmployeeResponsibilityResponseDto>> listByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(employeeResponsibilityService.listByEmployee(employeeId));
    }

    @GetMapping("/responsibilities/responsible/{responsibleEmployeeId}")
    @Operation(summary = "Get responsibilities by responsible employee", description = "Returns responsibilities where the specified employee is responsible")
    @ApiResponse(responseCode = "200", description = "Responsibilities successfully retrieved")
    public ResponseEntity<List<EmployeeResponsibilityResponseDto>> listByResponsible(
            @Parameter(description = "Responsible employee identifier", required = true)
            @PathVariable UUID responsibleEmployeeId) {
        return ResponseEntity.ok(employeeResponsibilityService.listByResponsible(responsibleEmployeeId));
    }

    @PatchMapping("/responsibilities/{responsibilityId}/close")
    @Operation(summary = "Close responsibility", description = "Closes an employee responsibility with the specified end date")
    @ApiResponse(responseCode = "200", description = "Responsibility successfully closed",
            content = @Content(schema = @Schema(implementation = EmployeeResponsibilityResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid end date",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Responsibility not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponsibilityResponseDto> close(@PathVariable UUID responsibilityId,
                                                                   @Parameter(description = "Responsibility end date", required = true)
                                                                   @RequestParam
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                   LocalDate endDate
    ) {
        return ResponseEntity.ok(employeeResponsibilityService.close(responsibilityId, endDate));
    }

    @PutMapping("/responsibilities/{responsibilityId}")
    @Operation(summary = "Update responsibility", description = "Updates an employee responsibility")
    @ApiResponse(responseCode = "200", description = "Responsibility successfully updated",
            content = @Content(schema = @Schema(implementation = EmployeeResponsibilityResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Responsibility not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeResponsibilityResponseDto> update(@PathVariable UUID responsibilityId,
                                                                    @Valid @RequestBody EmployeeResponsibilityUpdateRequestDto request) {
        return ResponseEntity.ok(employeeResponsibilityService.update(responsibilityId, request));
    }

    @DeleteMapping("/responsibilities/{responsibilityId}")
    @Operation(summary = "Delete responsibility", description = "Deletes an employee responsibility")
    @ApiResponse(responseCode = "204", description = "Responsibility successfully deleted")
    @ApiResponse(responseCode = "404", description = "Responsibility not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID responsibilityId) {
        employeeResponsibilityService.delete(responsibilityId);
        return ResponseEntity.noContent().build();
    }
}