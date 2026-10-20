package com.zodiakk.employeeservice.employee.controller;

import com.zodiakk.employeeservice.employee.dto.response.EmployeeRegistrationInfoDto;
import com.zodiakk.employeeservice.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/employees")
@RequiredArgsConstructor
public class InternalEmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}/account-info")
    public ResponseEntity<EmployeeRegistrationInfoDto> getRegistrationInfo(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(employeeService.getRegistrationInfo(id));
    }
}