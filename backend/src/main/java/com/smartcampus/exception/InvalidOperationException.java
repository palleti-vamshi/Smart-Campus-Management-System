package com.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested operation violates business rules.
 * Results in HTTP 400 Bad Request.
 *
 * Examples: invalid document status transition, marks exceeding max marks,
 * timetable scheduling conflict.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
