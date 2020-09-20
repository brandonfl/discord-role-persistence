package com.brandonfl.discordrolepersistence.utils;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import java.awt.Color;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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

  public static EmbedBuilder getGenericEmbed() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder
        .setColor(new Color(52, 152, 219))
        .setTimestamp(Instant.from(ZonedDateTime.now()))
        .setFooter("Discord Role Persistence");
    return embedBuilder;
  }

}
