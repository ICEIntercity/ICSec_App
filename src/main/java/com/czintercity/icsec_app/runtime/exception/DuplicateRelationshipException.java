package com.czintercity.icsec_app.runtime.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an attempt is made to create a control relationship that already exists between the same source and target.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRelationshipException extends RuntimeException {
  public DuplicateRelationshipException (String message) {
    super(message);
  }
}