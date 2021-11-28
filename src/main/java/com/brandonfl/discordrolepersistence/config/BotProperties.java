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

import com.brandonfl.discordrolepersistence.config.shared.ThreadConfig;
import lombok.AccessLevel;
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
    private String heartbeatStatusUrl;
    private String ownerId;
    private Long guidDevelopmentId;
    private Persistence persistence = new Persistence();

    @Data
    public static class Persistence {
      @Getter(AccessLevel.PRIVATE)
      private Boolean reloadAtBotReload;

      @Getter(AccessLevel.PRIVATE)
      private Boolean persistAtRoleChange;

      private ThreadConfig user = new ThreadConfig();
      private ThreadConfig server = new ThreadConfig();
      private ThreadConfig role = new ThreadConfig();

      public boolean needToReloadPersistenceAtBotReload() {
        return Boolean.TRUE.equals(getReloadAtBotReload());
      }

      public boolean needToPersistAtRoleChange() {
        return Boolean.TRUE.equals(getPersistAtRoleChange());
      }
    }
  }

}
