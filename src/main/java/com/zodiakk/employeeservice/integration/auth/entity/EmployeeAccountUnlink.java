package com.zodiakk.employeeservice.integration.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_account_unlinks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAccountUnlink {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "unlinked_at", nullable = false)
    private OffsetDateTime unlinkedAt;
}