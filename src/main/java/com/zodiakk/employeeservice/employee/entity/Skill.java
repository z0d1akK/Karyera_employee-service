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
@Table(name = "skills")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Skill extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Skill skill = (Skill) o;

        if (getId() != null && skill.getId() != null) {
            return Objects.equals(getId(), skill.getId());
        }

        return Objects.equals(name, skill.name);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(name);
    }
}