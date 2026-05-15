package com.legaldev.mvp.web;

import com.legaldev.mvp.config.AppProcessingProperties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Enforces per-request processing deadline ({@code app.processing.timeout-seconds}) for all MVP REST
 * handlers — see README “超时（HTTP）映射声明”.
 */
@Component
public class ProcessingBoundary {

  private final ExecutorService processingExecutor;
  private final AppProcessingProperties processingProperties;

  public ProcessingBoundary(
      @Qualifier("processingExecutor") ExecutorService processingExecutor,
      AppProcessingProperties processingProperties) {
    this.processingExecutor = processingExecutor;
    this.processingProperties = processingProperties;
  }

  public <T> T complete(Supplier<T> supplier) {
    Future<T> future = processingExecutor.submit(supplier::get);
    try {
      return future.get(processingProperties.timeoutSeconds(), TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new MvpRequestTimeoutException("MVP processing deadline exceeded", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      future.cancel(true);
      throw new MvpRequestTimeoutException("MVP processing interrupted", e);
    } catch (ExecutionException e) {
      Throwable c = e.getCause();
      if (c instanceof RuntimeException re) {
        throw re;
      }
      if (c instanceof Error err) {
        throw err;
      }
      throw new IllegalStateException(c);
    }
  }
}
