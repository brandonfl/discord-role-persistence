package com.brandonfl.discordrolepersistence.utils;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import java.awt.Color;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

public final class DiscordBotUtils {

  private DiscordBotUtils() {
  }

  public static final Color COLOR = new Color(108, 135, 202);

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
        .setColor(COLOR)
        .setTimestamp(Instant.from(ZonedDateTime.now()))
        .setFooter("Discord Role Persistence", jda.getSelfUser().getEffectiveAvatarUrl());
    return embedBuilder;
  }

  public static Paginator.Builder getGenericPaginatorBuilder(EventWaiter eventWaiter) {
    return new Paginator.Builder()
        .setColor(COLOR)
        .setColumns(1)
        .setItemsPerPage(10)
        .showPageNumbers(true)
        .waitOnSinglePage(false)
        .useNumberedItems(false)
        .setFinalAction(m -> {
          try {
            m.clearReactions().queue();
          } catch(PermissionException ex) {
            m.delete().queue();
          }
        })
        .setEventWaiter(eventWaiter)
        .setTimeout(1, TimeUnit.MINUTES);
  }

  public static boolean verifyCommandFormat(Message message, String expectedCommand) {
    return message.getContentRaw().matches("^[^\\s]+" + expectedCommand + "(\\s.*)?$");
  }

  public static boolean verifyCommand(ServerEntity serverEntity, Message message, String expectedCommand) {
    return message.getContentRaw().startsWith(serverEntity.getCommandPrefix() + expectedCommand);
  }

  public static int getUpperRole(List<Role> roles) {
    return roles.stream().mapToInt(Role::getPosition).max().orElse(-1);
  }

}
