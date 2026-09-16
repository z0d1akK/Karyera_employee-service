package com.zodiakk.employeeservice.employee.dto.request.update;

import com.zodiakk.employeeservice.common.validation.ValidationMessages;
import com.zodiakk.employeeservice.employee.entity.enums.MilitaryStatus;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilitaryObligationUpdateRequestDto {

    @NotNull(message = ValidationMessages.MILITARY_STATUS_REQUIRED)
    private MilitaryStatus status;

    @Size(max = 100, message = ValidationMessages.MILITARY_RANK_SIZE)
    private String militaryRank;

    @Size(max = 100, message = ValidationMessages.CATEGORY_SIZE)
    private String category;

    @Size(max = 100, message = ValidationMessages.DOCUMENT_NUMBER_SIZE)
    private String documentNumber;
}