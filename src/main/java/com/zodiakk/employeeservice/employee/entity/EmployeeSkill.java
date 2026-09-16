package com.zodiakk.employeeservice.employee.entity;

import com.zodiakk.employeeservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "employee_skills")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EmployeeSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "years_of_experience", precision = 4, scale = 2)
    private BigDecimal yearsOfExperience;

    @Column(name = "last_assessed_at")
    private LocalDate lastAssessedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EmployeeSkill that = (EmployeeSkill) o;

        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        return Objects.equals(employee, that.employee)
                && Objects.equals(skill, that.skill);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(employee, skill);
    }
}