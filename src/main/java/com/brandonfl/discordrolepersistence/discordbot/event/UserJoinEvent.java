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

package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class UserJoinEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;
  private final PersistExecutor persistExecutor;

  @Override
  public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
    if (event.getMember() != null) {
      persistExecutor.persistUser(event.getGuild(), event.getMember());
    }
  }

  @Override
  public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent joinEvent) {
    Optional<ServerUserEntity> serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(joinEvent.getMember().getIdLong(), joinEvent.getGuild().getIdLong());

    if (serverUserEntity.isPresent()) {
      Set<Role> rolesToAddToUser = serverUserEntity
          .get()
          .getRoleEntities()
          .stream()
          .filter(serverRoleEntity -> !serverRoleEntity.isBlacklisted())
          .map(serverRoleEntity -> joinEvent.getGuild().getRoleById(serverRoleEntity.getRoleGuid()))
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());

      StringBuilder logStringBuilder = new StringBuilder();
      StringBuilder welcomeBackStringBuilder = new StringBuilder();
      for (Role role : rolesToAddToUser) {
        if (role.hasPermission(Permission.ADMINISTRATOR)) {
          logStringBuilder.append(":no_entry: ").append(role.getName()).append(" (admin permissions are not backup)").append("\n");
        } else {
          boolean success = true;
          try {
            joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).complete();
          } catch (Exception e) {
            success = false;
          } finally {
            logStringBuilder
                .append(success ? ":white_check_mark: " : ":warning: ")
                .append(role.getName()).append(success ? "" : " (not enough permissions, please put the bot upper into the role hierarchy)")
                .append("\n");
            if (success) {
              welcomeBackStringBuilder.append("- ").append(role.getAsMention()).append("\n");
            }
          }
        }
      }

      ServerEntity serverEntity = serverUserEntity.get().getServerGuid();
      if (logStringBuilder.length() != 0) {
        EmbedBuilder logEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA());
        Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(joinEvent.getGuild(), serverEntity);
        if (logChannel.isPresent()) {
          logEmbedBuilder
              .setAuthor("Role backup for " + joinEvent.getMember().getEffectiveName(), null, joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .appendDescription("User id : " + joinEvent.getMember().getUser().getId() + "\n\n" + logStringBuilder.toString());

          logChannel.get().sendMessage(logEmbedBuilder.build()).queue();
        }
      }

      if (welcomeBackStringBuilder.length() != 0) {
        EmbedBuilder welcomeBackEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA());
        Optional<TextChannel> welcomeBackChannel = DiscordBotUtils.getWelcomeBackChannel(joinEvent.getGuild(), serverEntity);
        if (welcomeBackChannel.isPresent()) {
          welcomeBackEmbedBuilder
              .setTitle("Welcome back " + joinEvent.getMember().getEffectiveName())
              .setThumbnail(joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .addField("Here are your old roles that have been given back to you", welcomeBackStringBuilder.toString(), true);

          welcomeBackChannel.get().sendMessage(joinEvent.getMember().getAsMention()).queue();
          welcomeBackChannel.get().sendMessage(welcomeBackEmbedBuilder.build()).queue();
        }
      }
    }
  }
}
