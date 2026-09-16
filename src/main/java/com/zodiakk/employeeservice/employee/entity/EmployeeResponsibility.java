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

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "employee_responsibilities")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EmployeeResponsibility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsible_employee_id", nullable = false)
    private Employee responsibleEmployee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsibility_type_id", nullable = false)
    private ResponsibilityType responsibilityType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EmployeeResponsibility that = (EmployeeResponsibility) o;

        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        return Objects.equals(employee, that.employee)
                && Objects.equals(responsibleEmployee, that.responsibleEmployee)
                && Objects.equals(responsibilityType, that.responsibilityType)
                && Objects.equals(startDate, that.startDate);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(employee, responsibleEmployee, responsibilityType, startDate);
    }
}