package com.brandonfl.demobot.discordbot.model;

import lombok.Getter;

public enum BotConfig {
  MEMBER_COUNTER_CHANNEL("MEMBER_COUNTER_CHANNEL");

  @Getter
  private String token;

  BotConfig(String token) {
    this.token = token;
  }
}
