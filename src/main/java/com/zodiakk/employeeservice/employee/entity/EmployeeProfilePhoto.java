package com.zodiakk.employeeservice.employee.entity;

import com.zodiakk.employeeservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "employee_profile_photos")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EmployeeProfilePhoto extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "object_key", nullable = false, unique = true, length = 500)
    private String objectKey;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EmployeeProfilePhoto that = (EmployeeProfilePhoto) o;

        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        return Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public final int hashCode() {
        return getId() != null
                ? Objects.hash(getId())
                : Objects.hash(objectKey);
    }
}
