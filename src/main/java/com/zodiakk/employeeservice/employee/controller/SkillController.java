package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.employee.dto.request.create.SkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SkillFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SkillResponseDto;
import com.zodiakk.employeeservice.employee.service.SkillService;
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
@RequestMapping("/api/employees/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Operations for managing employee skills")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @Operation(summary = "Create skill", description = "Creates a new employee skill")
    @ApiResponse(responseCode = "201", description = "Skill successfully created",
            content = @Content(schema = @Schema(implementation = SkillResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Skill with the specified name already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<SkillResponseDto> create(@Valid @RequestBody SkillCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID")
    @ApiResponse(responseCode = "200", description = "Skill found",
            content = @Content(schema = @Schema(implementation = SkillResponseDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Skill not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<SkillResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(skillService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get skills", description = "Returns a paginated and filtered list of skills")
    @ApiResponse(responseCode = "200", description = "Skills successfully retrieved")
    public ResponseEntity<Page<SkillResponseDto>> findAll(@Valid SkillFilterDto filter, @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(skillService.findAll(filter, pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update skill")
    @ApiResponse(responseCode = "200", description = "Skill successfully updated",
            content = @Content(schema = @Schema(implementation = SkillResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Skill not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Skill with the specified name already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<SkillResponseDto> update(@PathVariable UUID id, @Valid @RequestBody SkillUpdateRequestDto request) {
        return ResponseEntity.ok(skillService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate skill")
    @ApiResponse(responseCode = "204", description = "Skill successfully activated")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        skillService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate skill")
    @ApiResponse(responseCode = "204", description = "Skill successfully deactivated")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        skillService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete skill", description = "Deletes a skill that is not assigned to employees")
    @ApiResponse(responseCode = "204", description = "Skill successfully deleted")
    @ApiResponse(responseCode = "404", description = "Skill not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Skill is currently assigned to employees",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}