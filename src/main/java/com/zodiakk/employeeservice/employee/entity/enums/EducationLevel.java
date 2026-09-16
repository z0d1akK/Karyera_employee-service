package com.zodiakk.employeeservice.employee.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationLevel {

    SECONDARY("Среднее"),
    VOCATIONAL("Среднее специальное"),
    BACHELOR("Бакалавр"),
    MASTER("Магистр"),
    DOCTORATE("Докторская степень"),
    OTHER("Другое");

    private final String displayName;
}