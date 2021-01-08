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

  @Scheduled(fixedDelay = 1000)
  public void sendHeartbeatStatus() {
    try {
      System.out.println("hello ?");
      URL url = new URL(botProperties.getSetting().getHeartbeatStatusUrl());
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      int statusCode = con.getResponseCode();
      System.out.println(statusCode);
      if (statusCode > 299) {
        throw new Exception(String.valueOf(statusCode));
      }
    } catch (Exception exception) {
      logger.error("Error code while sending heartbeat status : " + exception.getMessage(), exception);
    }
  }
}
