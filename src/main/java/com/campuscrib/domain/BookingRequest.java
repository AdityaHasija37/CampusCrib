package com.campuscrib.domain;

import java.time.Instant;
import java.time.LocalDate;

public record BookingRequest(
        long id,
        long apartmentId,
        String apartmentTitle,
        String apartmentCode,
        long studentId,
        String studentName,
        long agentId,
        LocalDate moveIn,
        int leaseMonths,
        String message,
        BookingStatus status,
        String agentNote,
        Instant createdAt,
        Instant decidedAt) {}
