package com.czintercity.icsec_app.assessment.exception;

/**
 * Thrown when a duplicate score entry is detected during assessment processing,
 * for example if the same control-technique pair appears more than once in a submission.
 */
public class DuplicateScoreException extends RuntimeException {
    public DuplicateScoreException(String message) {
        super(message);
    }
}
