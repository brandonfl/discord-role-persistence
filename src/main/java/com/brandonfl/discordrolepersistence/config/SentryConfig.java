package com.brandonfl.discordrolepersistence.config;

import io.sentry.spring.SentryExceptionResolver;
import io.sentry.spring.SentryServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SentryConfig {

  @Bean
  public HandlerExceptionResolver sentryExceptionResolver() {
    return new SentryExceptionResolver();
  }

  @Bean
  public ServletContextInitializer sentryServletContextInitializer() {
    return new SentryServletContextInitializer();
  }
}
