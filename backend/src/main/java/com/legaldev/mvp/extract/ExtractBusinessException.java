package com.legaldev.mvp.extract;

/** Structured extract failure aligned with {@code ExtractErrorResponse.errorCode}. */
public class ExtractBusinessException extends RuntimeException {

  private final String errorCode;

  public ExtractBusinessException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String errorCode() {
    return errorCode;
  }
}
