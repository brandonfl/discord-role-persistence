package com.brandonfl.demobot.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bot", ignoreInvalidFields = false)
@PropertySources({
    @PropertySource(value = "classpath:META-INF/additional-spring-configuration-metadata.json", ignoreResourceNotFound = true)
})
public class BotProperties {
  @Getter
  private final Setting setting = new Setting();

  @Data
  public static class Setting {
    private String version;
    private String token;
  }

}
