package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import com.zodiakk.employeeservice.employee.service.EmployeeProfilePhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees/{employeeId}/profile-photo")
@RequiredArgsConstructor
@Tag(name = "Employee Profile Photo", description = "Operations for managing employee profile photos")
public class EmployeeProfilePhotoController {

    private final EmployeeProfilePhotoService profilePhotoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo", description = "Uploads a profile photo for an employee")
    @ApiResponse(responseCode = "200", description = "Profile photo successfully uploaded",
            content = @Content(schema = @Schema(implementation = EmployeeProfilePhotoResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid file",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeProfilePhotoResponseDto> upload(
            @Parameter(description = "Employee identifier", required = true) @PathVariable UUID employeeId,
            @Parameter(description = "Employee profile photo", required = true) @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profilePhotoService.upload(employeeId, file));
    }

    @GetMapping
    @Operation(summary = "Get profile photo", description = "Returns the profile photo information of an employee")
    @ApiResponse(responseCode = "200", description = "Profile photo found",
            content = @Content(schema = @Schema(implementation = EmployeeProfilePhotoResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Profile photo not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<EmployeeProfilePhotoResponseDto> getByEmployeeId(
            @Parameter(description = "Employee identifier", required = true) @PathVariable UUID employeeId) {
        return ResponseEntity.ok(profilePhotoService.getByEmployeeId(employeeId));
    }

    @DeleteMapping
    @Operation(summary = "Delete profile photo", description = "Deletes the profile photo of an employee")
    @ApiResponse(responseCode = "204", description = "Profile photo successfully deleted")
    @ApiResponse(responseCode = "404", description = "Profile photo not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID employeeId) {
        profilePhotoService.delete(employeeId);
        return ResponseEntity.noContent().build();
    }
}
