package com.zodiakk.employeeservice.employee.entity;

import com.zodiakk.employeeservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "grades")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Grade extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private Integer level;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Grade grade = (Grade) o;

        if (getId() != null && grade.getId() != null) {
            return Objects.equals(getId(), grade.getId());
        }

        return Objects.equals(name, grade.name);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(name);
    }
}