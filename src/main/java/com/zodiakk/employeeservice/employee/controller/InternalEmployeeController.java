package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.employee.dto.request.EmployeeLookupRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.ReferenceLookupRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeAccessScopeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeLookupItemDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeLookupResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeRegistrationInfoDto;
import com.zodiakk.employeeservice.employee.dto.response.ReferenceLookupResponseDto;
import com.zodiakk.employeeservice.employee.service.EmployeeService;
import com.zodiakk.employeeservice.employee.service.InternalEmployeeReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
@Tag(name = "Internal Employees", description = "Service-to-service employee contracts")
public class InternalEmployeeController {

    private final EmployeeService employeeService;

    private final InternalEmployeeReferenceService referenceService;

    @GetMapping("/employees/{id}/account-info")
    public ResponseEntity<EmployeeRegistrationInfoDto> getRegistrationInfo(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(employeeService.getRegistrationInfo(id));
    }

    @PostMapping("/employees/lookup")
    @Operation(summary = "Batch employee lookup", description = "Returns reference data for the requested employee identifiers")
    @ApiResponse(responseCode = "200", description = "Employees resolved",
            content = @Content(schema = @Schema(implementation = EmployeeLookupResponseDto.class)))
    public ResponseEntity<EmployeeLookupResponseDto> lookup(
            @Valid @RequestBody EmployeeLookupRequestDto request
    ) {
        return ResponseEntity.ok(employeeService.lookup(request.employeeIds()));
    }

    @PostMapping("/positions/lookup")
    @Operation(summary = "Batch position lookup", description = "Returns names and activity flags for the requested position identifiers")
    @ApiResponse(responseCode = "200", description = "Positions resolved",
            content = @Content(schema = @Schema(implementation = ReferenceLookupResponseDto.class)))
    public ResponseEntity<ReferenceLookupResponseDto> lookupPositions(
            @Valid @RequestBody ReferenceLookupRequestDto request
    ) {
        return ResponseEntity.ok(referenceService.lookupPositions(request.ids()));
    }

    @PostMapping("/grades/lookup")
    @Operation(summary = "Batch grade lookup", description = "Returns names and activity flags for the requested grade identifiers")
    @ApiResponse(responseCode = "200", description = "Grades resolved",
            content = @Content(schema = @Schema(implementation = ReferenceLookupResponseDto.class)))
    public ResponseEntity<ReferenceLookupResponseDto> lookupGrades(
            @Valid @RequestBody ReferenceLookupRequestDto request
    ) {
        return ResponseEntity.ok(referenceService.lookupGrades(request.ids()));
    }

    @PostMapping("/skills/lookup")
    @Operation(summary = "Batch skill lookup", description = "Returns names and activity flags for the requested skill identifiers")
    @ApiResponse(responseCode = "200", description = "Skills resolved",
            content = @Content(schema = @Schema(implementation = ReferenceLookupResponseDto.class)))
    public ResponseEntity<ReferenceLookupResponseDto> lookupSkills(
            @Valid @RequestBody ReferenceLookupRequestDto request
    ) {
        return ResponseEntity.ok(referenceService.lookupSkills(request.ids()));
    }

    @GetMapping("/employees/{employeeId}/access-scope")
    @Operation(summary = "Resolve employee access scope",
            description = "Tells whether the acting employee is the target employee or their manager")
    @ApiResponse(responseCode = "200", description = "Access scope resolved",
            content = @Content(schema = @Schema(implementation = EmployeeAccessScopeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<EmployeeAccessScopeResponseDto> getAccessScope(
            @Parameter(description = "Target employee identifier", required = true)
            @PathVariable UUID employeeId,
            @Parameter(description = "Acting employee identifier")
            @RequestParam(required = false) UUID actorEmployeeId
    ) {
        return ResponseEntity.ok(referenceService.getAccessScope(employeeId, actorEmployeeId));
    }

    @GetMapping("/employees/by-user/{userId}")
    @Operation(summary = "Employee lookup by user id", description = "Resolves the employee attached to the given auth user")
    @ApiResponse(responseCode = "200", description = "Employee resolved",
            content = @Content(schema = @Schema(implementation = EmployeeLookupItemDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<EmployeeLookupItemDto> getByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(employeeService.getLookupByUserId(userId));
    }
}
