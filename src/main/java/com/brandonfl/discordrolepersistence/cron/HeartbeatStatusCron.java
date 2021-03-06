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

package com.brandonfl.discordrolepersistence.cron;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${bot.setting.heartbeatStatusUrl:}')")
@EnableScheduling
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeartbeatStatusCron {
  private static final Logger logger = LoggerFactory.getLogger(HeartbeatStatusCron.class);

  private final BotProperties botProperties;

  @Scheduled(fixedDelay = 600000)
  public void sendHeartbeatStatus() {
    try {
      URL url = new URL(botProperties.getSetting().getHeartbeatStatusUrl());
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      int statusCode = con.getResponseCode();
      if (statusCode > 299) {
        throw new Exception(String.valueOf(statusCode));
      }
    } catch (Exception exception) {
      logger.error("Error code while sending heartbeat status : " + exception.getMessage(), exception);
    }
  }
}
