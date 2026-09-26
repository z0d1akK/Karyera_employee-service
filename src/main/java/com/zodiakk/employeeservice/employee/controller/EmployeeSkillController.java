package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeSkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeSkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSkillResponseDto;
import com.zodiakk.employeeservice.employee.service.EmployeeSkillService;
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
@RequestMapping("/api/employees/{employeeId}/skills")
@RequiredArgsConstructor
@Tag(name = "Employee Skills", description = "Operations for managing employee skills")
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    @PostMapping
    @Operation(summary = "Add skill to employee", description = "Assigns a skill to an employee")
    @ApiResponse(responseCode = "201", description = "Skill successfully assigned",
            content = @Content(schema = @Schema(implementation = EmployeeSkillResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee or skill not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Skill is already assigned to the employee",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeSkillResponseDto> add(@Parameter(description = "Employee identifier", required = true) @PathVariable UUID employeeId,
            @Valid @RequestBody EmployeeSkillCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeSkillService.add(employeeId, request));
    }

    @GetMapping
    @Operation(summary = "Get employee skills", description = "Returns all skills assigned to an employee")
    @ApiResponse(responseCode = "200", description = "Employee skills successfully retrieved")
    public ResponseEntity<List<EmployeeSkillResponseDto>> listByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(employeeSkillService.listByEmployee(employeeId));
    }

    @PutMapping("/{employeeSkillId}")
    @Operation(summary = "Update employee skill", description = "Updates an employee skill assignment")
    @ApiResponse(responseCode = "200", description = "Employee skill successfully updated",
            content = @Content(schema = @Schema(implementation = EmployeeSkillResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee skill not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeSkillResponseDto> update(@PathVariable UUID employeeId, @PathVariable UUID employeeSkillId,
            @Valid @RequestBody EmployeeSkillUpdateRequestDto request) {
        return ResponseEntity.ok(employeeSkillService.update(employeeId, employeeSkillId, request));
    }

    @DeleteMapping("/{skillId}")
    @Operation(summary = "Remove skill from employee", description = "Removes a skill assignment from an employee")
    @ApiResponse(responseCode = "204", description = "Skill successfully removed")
    @ApiResponse(responseCode = "404", description = "Employee or skill assignment not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> remove(@PathVariable UUID employeeId, @PathVariable UUID skillId) {
        employeeSkillService.remove(employeeId, skillId);
        return ResponseEntity.noContent().build();
    }
}

