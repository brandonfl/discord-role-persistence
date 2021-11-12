package com.brandonfl.discordrolepersistence.config.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bot-datasource", ignoreInvalidFields = false)
@PropertySources({
    @PropertySource(value = "classpath:META-INF/additional-datasource-configuration-metadata.json", ignoreResourceNotFound = true)
})
@Data
public class DataSourceProperties {
  private String host;
  private String name;
  private String username;
  private String password;
  private String timezone;
  private String path;
}
