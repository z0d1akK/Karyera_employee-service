package com.zodiakk.employeeservice.employee.repository.specification.skill;

import com.zodiakk.employeeservice.employee.dto.request.filter.SkillFilterDto;
import com.zodiakk.employeeservice.employee.entity.Skill;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class SkillSpecification {

    public static Specification<Skill> filter(SkillFilterDto filter) {
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

            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("category")),
                                "%" + filter.getCategory().trim().toLowerCase() + "%"
                        )
                );
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
