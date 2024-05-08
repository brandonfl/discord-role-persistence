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

package com.brandonfl.discordrolepersistence.service;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggerService {

  private final RepositoryContainer repositoryContainer;

  public void logRolesGivedBack(GuildMemberJoinEvent joinEvent, Long serverGuid, Set<Role> roles) {
    ServerEntity serverEntity = repositoryContainer.getServerRepository().findByGuid(serverGuid).orElse(null);
    if (serverEntity == null) {
      return;
    }

    if (!roles.isEmpty() && (serverEntity.getLogChannel() != null || serverEntity.getWelcomeBackChannel() != null)) {
      final StringBuilder rolesGivedBackStringBuilder = new StringBuilder();
      roles.forEach(role -> rolesGivedBackStringBuilder.append("- ").append(role.getAsMention()).append("\n"));

      if (serverEntity.getLogChannel() != null) {
        Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(joinEvent.getGuild(), serverEntity);
        if (logChannel.isPresent()) {
          EmbedBuilder logEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA())
              .setAuthor("Role backup for " + joinEvent.getMember().getEffectiveName(), null, joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .appendDescription("User id : " + joinEvent.getMember().getUser().getId() + "\n\n" + rolesGivedBackStringBuilder.toString());

          logChannel.get().sendMessage(logEmbedBuilder.build()).queue();
        }
      }

      if (serverEntity.getWelcomeBackChannel() != null) {
        Optional<TextChannel> welcomeBackChannel = DiscordBotUtils.getWelcomeBackChannel(joinEvent.getGuild(), serverEntity);
        if (welcomeBackChannel.isPresent()) {
          EmbedBuilder welcomeBackEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA())
              .setTitle("Welcome back " + joinEvent.getMember().getEffectiveName())
              .setThumbnail(joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .addField("Here are your old roles that have been given back to you", rolesGivedBackStringBuilder.toString(), true);

          welcomeBackChannel.get().sendMessage(joinEvent.getMember().getAsMention()).queue();
          welcomeBackChannel.get().sendMessage(welcomeBackEmbedBuilder.build()).queue();
        }
      }
    }
  }

  public void logUserRoleUpdate(
      @Nonnull GenericGuildEvent event,
      @Nonnull Member member,
      @Nonnull List<Role> roles,
      @Nonnull final String fieldName) {
    Optional<ServerUserEntity> serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(member.getIdLong(), event.getGuild().getIdLong());

    if (serverUserEntity.isPresent()) {
      Optional<TextChannel> textChannel = DiscordBotUtils.getLogChannel(event.getGuild(), serverUserEntity.get().getServerGuid());
      if (textChannel.isPresent()) {
        EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(event.getJDA());

        embedBuilder
            .setDescription("user id : " + member.getUser().getId())
            .setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl())
            .addField(fieldName, roles.stream().map(
                Role::getName).collect(Collectors.joining("\n")), true);

        textChannel.get().sendMessage(embedBuilder.build()).queue();
      }
    }
  }

  public void logServerRole(
      ServerEntity serverEntity,
      GenericRoleEvent roleEvent,
      String message) {
    Optional<TextChannel> textChannel = DiscordBotUtils.getLogChannel(roleEvent.getGuild(), serverEntity);
    if (textChannel.isPresent()) {
      EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(roleEvent.getJDA());
      embedBuilder
          .addField(message,
              roleEvent.getRole().getName() + " (" + roleEvent.getRole().getId() + ")",
              true);

      textChannel.get().sendMessage(embedBuilder.build()).queue();
    }
  }
}
