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

import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.service.LoggerService;
import com.brandonfl.discordrolepersistence.service.PersistenceService;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MemberEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;
  private final PersistenceService persistenceService;
  private final LoggerService loggerService;

  @Override
  public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
    if (event.getMember() != null) {
      persistenceService.persistUser(event.getGuild(), event.getMember());
    }
  }

  @Override
  public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent joinEvent) {
    ServerUserEntity serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(joinEvent.getMember().getIdLong(), joinEvent.getGuild().getIdLong())
        .orElse(null);

    if (serverUserEntity != null && serverUserEntity.getRoleEntities() != null && !serverUserEntity.getRoleEntities().isEmpty()) {
      final int botUpperRole = DiscordBotUtils.getUpperRole(joinEvent.getGuild().getSelfMember().getRoles());
      final Set<Role> rolesToAddToUser = serverUserEntity
          .getRoleEntities()
          .stream()
          .filter(serverRoleEntity -> !serverRoleEntity.isBlacklisted())
          .map(serverRoleEntity -> joinEvent.getGuild().getRoleById(serverRoleEntity.getRoleGuid()))
          .filter(role -> role != null && !role.hasPermission(Permission.ADMINISTRATOR) && (botUpperRole > role.getPosition()))
          .collect(Collectors.toSet());

      rolesToAddToUser.forEach(role -> {
        joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).queue();
      });

      loggerService.logRolesGivedBack(joinEvent, serverUserEntity.getServerGuid(), rolesToAddToUser);
    }
  }
}
