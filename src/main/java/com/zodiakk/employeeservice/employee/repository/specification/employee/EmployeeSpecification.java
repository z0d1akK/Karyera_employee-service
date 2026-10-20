package com.zodiakk.employeeservice.employee.repository.specification.employee;

import com.zodiakk.employeeservice.employee.dto.request.filter.EmployeeFilterDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeResponsibility;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class EmployeeSpecification {

    public static Specification<Employee> filter(EmployeeFilterDto filter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + filter.getFirstName().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getLastName() != null && !filter.getLastName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + filter.getLastName().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("id"), filter.getDepartmentId()));
            }

            if (filter.getPositionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("position").get("id"), filter.getPositionId()));
            }

            if (filter.getGradeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("grade").get("id"), filter.getGradeId()));
            }

            if (filter.getSpecializationId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("specialization").get("id"),
                        filter.getSpecializationId()
                ));
            }

            if (filter.getEmploymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("employmentStatus"), filter.getEmploymentStatus()));
            }

            if (filter.getHireDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("hireDate"), filter.getHireDateFrom()));
            }

            if (filter.getHireDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), filter.getHireDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employee> subordinatesOf(UUID managerUserId) {
        return (root, query, criteriaBuilder) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<EmployeeResponsibility> responsibility = subquery.from(EmployeeResponsibility.class);

            subquery.select(responsibility.get("employee").get("id"))
                    .where(
                            criteriaBuilder.equal(
                                    responsibility.get("responsibleEmployee").get("userId"),
                                    managerUserId
                            ),
                            criteriaBuilder.isNull(responsibility.get("endDate"))
                    );

            return root.get("id").in(subquery);
        };
    }
}
