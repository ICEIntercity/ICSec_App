package com.czintercity.icsec_app.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a coverage calculation is attempted on an {@link com.czintercity.icsec_app.assessment.entity.Assessment}
 * that has no control status mapping, making scoring impossible.
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BlankAssessmentException extends RuntimeException {
    public BlankAssessmentException(String message) {
        super(message);
    }
}