/*
 * MIT License
 *
 * Copyright (c) 2021 Fontany--Legall Brandon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.brandonfl.discordrolepersistence.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncExecutorConfiguration {

  @Bean(name = "userPersistenceExecutor")
  @Autowired
  public Executor userPersistenceExecutor(BotProperties botProperties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(botProperties.getSetting().getPersistence().getUser().getThreadNumber());
    executor.setThreadNamePrefix("UserPersistenceThread-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "rolePersistenceExecutor")
  @Autowired
  public Executor rolePersistenceExecutor(BotProperties botProperties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(botProperties.getSetting().getPersistence().getRole().getThreadNumber());
    executor.setThreadNamePrefix("RolePersistenceThread-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "serverPersistenceExecutor")
  @Autowired
  public Executor serverPersistenceExecutor(BotProperties botProperties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(botProperties.getSetting().getPersistence().getServer().getThreadNumber());
    executor.setThreadNamePrefix("ServerPersistenceThread-");
    executor.initialize();
    return executor;
  }
}
