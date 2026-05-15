package com.legaldev.mvp.web;

import com.legaldev.mvp.extract.ExtractBusinessException;
import com.legaldev.mvp.web.dto.ErrorResponse;
import com.legaldev.mvp.web.dto.ExtractErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MvpRequestTimeoutException.class)
  public ResponseEntity<ErrorResponse> onTimeout(MvpRequestTimeoutException ex) {
    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
        .body(
            new ErrorResponse(
                "REQUEST_TIMEOUT",
                "单次处理超过服务器时限（与 README 声明的 ≤10s 预算一致）。可缩小输入后重试。"));
  }

  @ExceptionHandler(ExtractBusinessException.class)
  public ResponseEntity<ExtractErrorResponse> onExtract(ExtractBusinessException ex) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ExtractErrorResponse(ex.errorCode(), ex.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> onValidation(MethodArgumentNotValidException ex) {
    String msg =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .orElse("请求体验证失败");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("BAD_REQUEST", msg));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> onUnreadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("BAD_REQUEST", "请求体不是合法 JSON"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> onStatus(ResponseStatusException ex) {
    int code = ex.getStatusCode().value();
    String msg = ex.getReason() != null ? ex.getReason() : "请求被拒绝";
    return ResponseEntity.status(code).body(new ErrorResponse("BAD_REQUEST", msg));
  }
}
