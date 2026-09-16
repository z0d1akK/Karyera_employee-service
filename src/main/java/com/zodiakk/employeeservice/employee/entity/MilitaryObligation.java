package com.zodiakk.employeeservice.employee.entity;

import com.zodiakk.employeeservice.common.entity.BaseEntity;
import com.zodiakk.employeeservice.employee.entity.enums.MilitaryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "military_obligations")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MilitaryObligation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MilitaryStatus status;

    @Column(name = "military_rank", length = 100)
    private String militaryRank;

    @Column(length = 100)
    private String category;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MilitaryObligation that = (MilitaryObligation) o;

        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        return Objects.equals(employee, that.employee);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(employee);
    }
}