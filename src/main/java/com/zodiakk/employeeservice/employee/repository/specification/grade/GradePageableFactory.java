package com.zodiakk.employeeservice.employee.repository.specification.grade;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

@UtilityClass
public class GradePageableFactory {

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "name", "name",
            "level", "level",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "active", "active"
    );

    public static Pageable create(PageRequestDto pageRequest) {

        String sortField = ALLOWED_SORT_FIELDS.getOrDefault(
                pageRequest.getSortBy(),
                "name"
        );

        Sort.Direction direction = pageRequest.getSortDirection() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), Sort.by(direction, sortField));
    }
}
