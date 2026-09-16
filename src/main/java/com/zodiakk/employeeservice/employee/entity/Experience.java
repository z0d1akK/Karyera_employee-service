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
@Table(name = "experiences")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Experience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(nullable = false, length = 150)
    private String position;

    @Column(length = 1000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Experience that = (Experience) o;

        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        return Objects.equals(employee, that.employee)
                && Objects.equals(company, that.company)
                && Objects.equals(position, that.position)
                && Objects.equals(startDate, that.startDate);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(employee, company, position, startDate);
    }
}