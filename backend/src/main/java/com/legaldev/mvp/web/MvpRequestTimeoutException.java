package com.legaldev.mvp.web;

/** Surfaces deterministic server-side deadline overruns (mapped to HTTP 504 + {@code ErrorResponse}). */
public class MvpRequestTimeoutException extends RuntimeException {

  public MvpRequestTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
