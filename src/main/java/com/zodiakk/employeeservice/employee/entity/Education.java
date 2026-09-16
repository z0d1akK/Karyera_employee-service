package com.zodiakk.employeeservice.employee.entity;

import com.zodiakk.employeeservice.common.entity.BaseEntity;
import com.zodiakk.employeeservice.employee.entity.enums.EducationLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "educations")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Education extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false, length = 30)
    private EducationLevel educationLevel;

    @Column(name = "institution_name", nullable = false, length = 250)
    private String institutionName;

    @Column(name = "field_of_study", length = 200)
    private String fieldOfStudy;

    @Column(name = "degree_name", length = 200)
    private String degreeName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Education that = (Education) o;

        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        return Objects.equals(employee, that.employee)
                && Objects.equals(institutionName, that.institutionName)
                && Objects.equals(educationLevel, that.educationLevel)
                && Objects.equals(startDate, that.startDate);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(employee, institutionName, educationLevel, startDate);
    }
}