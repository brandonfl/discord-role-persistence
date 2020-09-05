package com.brandonfl.demobot.db.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "discord_bot_config")
public class DiscordBotConfigEntity implements Serializable {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "token", nullable = false)
  @NotNull
  private String token = "";

  @Column(name = "guid", nullable = false)
  @NotNull
  private String guid = "";

  @Column(name = "value", nullable = false)
  @NotNull
  private String value = "";
}
