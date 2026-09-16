package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsibilityTypeCreateRequestDto {

    @NotBlank(message = ValidationMessages.CODE_REQUIRED)
    @Size(min = 2, max = 50, message = ValidationMessages.CODE_SIZE)
    private String code;

    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(min = 2, max = 150, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Size(max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = ValidationMessages.MULTIPLE_ALLOWED_REQUIRED)
    private Boolean multipleAllowed;
}