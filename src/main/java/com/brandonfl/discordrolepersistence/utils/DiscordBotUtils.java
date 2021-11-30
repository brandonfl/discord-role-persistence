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

package com.brandonfl.discordrolepersistence.utils;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.discordbot.DiscordBot;
import com.jagrosh.jdautilities.command.CommandEvent;
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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.managers.Presence;

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

  public static int getUpperRole(List<Role> roles) {
    return roles.stream().mapToInt(Role::getPosition).max().orElse(-1);
  }

  public static void updateJDAStatus(JDA jda, boolean reloading) {
    Presence presence = jda.getPresence();
    if (reloading) {
      presence.setPresence(
          OnlineStatus.DO_NOT_DISTURB,
          Activity.playing("reloading..."));
    } else {
      presence.setPresence(
          OnlineStatus.ONLINE,
          DiscordBot.DEFAULT_ACTIVITY);
    }
  }

  public static boolean isArgAnId(CommandEvent commandEvent) {
    return commandEvent.getArgs().matches("^[0-9]+$");
  }
}
