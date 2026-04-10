package com.czintercity.icsec_app.runtime.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRelationshipException extends RuntimeException {
  public DuplicateRelationshipException (String message) {
    super(message);
  }
}