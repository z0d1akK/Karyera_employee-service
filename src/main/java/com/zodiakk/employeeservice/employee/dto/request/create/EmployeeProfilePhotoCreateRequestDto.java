package com.zodiakk.employeeservice.employee.dto.request.create;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfilePhotoCreateRequestDto {

    @NotBlank(message = ValidationMessages.FILE_NAME_REQUIRED)
    @Size(max = 255, message = ValidationMessages.FILE_NAME_SIZE)
    private String fileName;

    @NotBlank(message = ValidationMessages.CONTENT_TYPE_REQUIRED)
    @Size(max = 100, message = ValidationMessages.CONTENT_TYPE_SIZE)
    private String contentType;

    @NotBlank(message = ValidationMessages.OBJECT_KEY_REQUIRED)
    @Size(max = 500, message = ValidationMessages.OBJECT_KEY_SIZE)
    private String objectKey;

    @NotNull(message = ValidationMessages.FILE_SIZE_REQUIRED)
    @Positive(message = ValidationMessages.FILE_SIZE_MIN)
    private Long fileSize;
}