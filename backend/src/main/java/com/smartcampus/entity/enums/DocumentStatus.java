package com.smartcampus.entity.enums;

/**
 * Status of a digital document request.
 * Defines the document lifecycle workflow:
 * SUBMITTED → UNDER_REVIEW → APPROVED → ISSUED
 *                          → REJECTED (terminal)
 */
public enum DocumentStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    ISSUED
}
