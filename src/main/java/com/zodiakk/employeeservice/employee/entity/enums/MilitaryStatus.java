package com.zodiakk.employeeservice.employee.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MilitaryStatus {

    NOT_APPLICABLE("Не военнообязанный"),
    LIABLE("Военнообязанный"),
    COMPLETED("Военную службу прошёл"),
    EXEMPT("Освобождён от военной службы"),
    DEFERRED("Имеет отсрочку");

    private final String displayName;
}