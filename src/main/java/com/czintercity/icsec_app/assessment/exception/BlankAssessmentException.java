package com.czintercity.icsec_app.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BlankAssessmentException extends RuntimeException {
    public BlankAssessmentException(String message) {
        super(message);
    }
}