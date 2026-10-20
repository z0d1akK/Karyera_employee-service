package com.zodiakk.employeeservice.outbox.entity;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED
}