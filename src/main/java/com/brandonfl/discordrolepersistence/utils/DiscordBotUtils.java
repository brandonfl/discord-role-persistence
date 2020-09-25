package com.brandonfl.discordrolepersistence.utils;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import java.awt.Color;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public final class DiscordBotUtils {

  private DiscordBotUtils() {
  }

  public static Optional<TextChannel> getLogChannel(Guild guild, ServerEntity serverEntity) {
    if (serverEntity.getLogChannel() != null) {
      return Optional.ofNullable(guild.getTextChannelById(serverEntity.getLogChannel()));
    } else {
      return Optional.empty();
    }
  }

  public static Optional<TextChannel> getWelcomeBackChannel(Guild guild, ServerEntity serverEntity) {
    if (serverEntity.getWelcomeBackChannel() != null) {
      return Optional.ofNullable(guild.getTextChannelById(serverEntity.getWelcomeBackChannel()));
    } else {
      return Optional.empty();
    }
  }

  public static EmbedBuilder getGenericEmbed(JDA jda) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder
        .setColor(new Color(108, 135, 202))
        .setTimestamp(Instant.from(ZonedDateTime.now()))
        .setFooter("Discord Role Persistence", jda.getSelfUser().getEffectiveAvatarUrl());
    return embedBuilder;
  }

  public static boolean verifyCommandFormat(Message message, String expectedCommand) {
    return message.getContentRaw().matches("^[^\\s]+" + expectedCommand + "(\\s.*)?$");
  }

  public static boolean verifyCommand(ServerEntity serverEntity, Message message, String expectedCommand) {
    return message.getContentRaw().startsWith(serverEntity.getCommandPrefix() + expectedCommand);
  }

}
