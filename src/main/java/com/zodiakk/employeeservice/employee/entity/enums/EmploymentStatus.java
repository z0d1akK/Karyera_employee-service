package com.zodiakk.employeeservice.employee.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentStatus {

    ACTIVE("Работает"),
    ON_LEAVE("В отпуске"),
    SUSPENDED("Отстранён"),
    TERMINATED("Уволен");

    private final String displayName;
}