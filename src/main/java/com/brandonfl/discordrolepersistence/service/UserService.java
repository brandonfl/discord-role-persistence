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
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

  private final RepositoryContainer repositoryContainer;
  private final LoggerService loggerService;
  private final ServerService serverService;

  @Async("userPersistenceExecutor")
  @Transactional
  public void persistUser(@Nonnull Guild guild, @Nonnull Member member) {
    Optional<ServerUserEntity> serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(member.getIdLong(), guild.getIdLong());

    if (!serverUserEntity.isPresent()) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(guild.getIdLong());
      if (!serverEntity.isPresent()) {
        serverService.persistNewServer(guild);
        return;
      } else {
        ServerUserEntity serverUserEntityToCreate = new ServerUserEntity();
        serverUserEntityToCreate.setServerGuid(serverEntity.get());
        serverUserEntityToCreate.setUserGuid(member.getIdLong());

        serverUserEntity = Optional.of(repositoryContainer.getServerUserRepository().save(serverUserEntityToCreate));
      }
    }

    List<Long> memberRoleIds = member.getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());

    ServerUserEntity userEntity = serverUserEntity.get();
    userEntity.setRoleEntities(userEntity
        .getServerGuid()
        .getRoleEntities()
        .stream()
        .filter(serverRoleEntity -> memberRoleIds.contains(serverRoleEntity.getRoleGuid()))
        .collect(Collectors.toSet()));

    repositoryContainer.getServerUserRepository().save(userEntity);
  }

  @Async("userPersistenceExecutor")
  public void backupRoles(@Nonnull GuildMemberJoinEvent joinEvent) {
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
