package com.zodiakk.employeeservice.employee.repository.specification.grade;

import com.zodiakk.employeeservice.employee.dto.request.filter.GradeFilterDto;
import com.zodiakk.employeeservice.employee.entity.Grade;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class GradeSpecification {

    public static Specification<Grade> filter(GradeFilterDto filter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + filter.getName().trim().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("level"), filter.getLevel()));
            }

            if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                "%" + filter.getDescription().trim().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), filter.getActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
