package com.legaldev.mvp.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bounded pool for deadline-wrapped work. Sized for ~20 concurrent MVP requests per
 * docs/_架构说明_.md (avoid unbounded queue growth under load).
 */
@Configuration
public class ProcessingExecutorConfig {

  @Bean(name = "processingExecutor", destroyMethod = "shutdown")
  ExecutorService processingExecutor() {
    int n = 20;
    return new ThreadPoolExecutor(
        n,
        n,
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(200),
        r -> {
          Thread t = new Thread(r);
          t.setName("mvp-processing-" + t.hashCode());
          t.setDaemon(true);
          return t;
        },
        new ThreadPoolExecutor.CallerRunsPolicy());
  }
}
