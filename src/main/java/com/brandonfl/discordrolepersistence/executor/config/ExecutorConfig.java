package com.brandonfl.discordrolepersistence.executor.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ExecutorConfig {

  @Bean(name = "asyncPersistExecutor")
  public Executor asyncPersistExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("PersistExecutor-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "asyncJoinExecutor")
  public Executor asyncJoinExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("JoinExecutor-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "asyncCommandExecutor")
  public Executor asyncCommandExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("CommandExecutor-");
    executor.initialize();
    return executor;
  }

}
