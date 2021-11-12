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
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServerService {

  private final RepositoryContainer repositoryContainer;
  private final LoggerService loggerService;

  @Transactional
  @Async("serverPersistenceExecutor")
  public void persistNewServer(@Nonnull Guild guild) {
    ServerEntity serverEntity = new ServerEntity();
    serverEntity.setGuid(guild.getIdLong());

    serverEntity = repositoryContainer.getServerRepository().save(serverEntity);

    Set<ServerRoleEntity> roles = new HashSet<>();
    for (Role role : guild.getRoles()) {
      ServerRoleEntity serverRoleEntity = new ServerRoleEntity();
      serverRoleEntity.setServerGuid(serverEntity);
      serverRoleEntity.setRoleGuid(role.getIdLong());

      roles.add(serverRoleEntity);
    }
    List<ServerRoleEntity> createdRoles = repositoryContainer.getServerRoleRepository().saveAll(roles);

    Set<ServerUserEntity> users = new HashSet<>();
    for (Member member : guild.getMembers()) {
      if (!member.isFake() && !member.getUser().isBot()) {
        Set<Long> memberRoleIds = member.getRoles().stream().map(Role::getIdLong).collect(
            Collectors.toSet());

        ServerUserEntity serverUserEntity = new ServerUserEntity();
        serverUserEntity.setServerGuid(serverEntity);
        serverUserEntity.setUserGuid(member.getIdLong());
        serverUserEntity.setRoleEntities(createdRoles.stream().filter(role -> memberRoleIds
            .contains(role.getRoleGuid()))
            .collect(Collectors.toSet()));

        users.add(serverUserEntity);
      }
    }
    repositoryContainer.getServerUserRepository().saveAll(users);
  }

  @Transactional
  @Async("rolePersistenceExecutor")
  public void createNewRoles(RoleCreateEvent event) {
    Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(event.getGuild().getIdLong());
    if (!serverEntity.isPresent()) {
      persistNewServer(event.getGuild());
    } else {
      ServerRoleEntity serverRoleEntity = repositoryContainer
          .getServerRoleRepository()
          .findByRoleGuidAndServerGuid(event.getRole().getIdLong(), event.getGuild().getIdLong())
          .orElse(null);

      if (serverRoleEntity == null) {
        serverRoleEntity = new ServerRoleEntity();
        serverRoleEntity.setServerGuid(serverEntity.get());
        serverRoleEntity.setRoleGuid(event.getGuild().getIdLong());

        repositoryContainer.getServerRoleRepository().save(serverRoleEntity);
      }

      loggerService.logServerRole(serverEntity.get(), event, ":white_check_mark: Created new role");
    }
  }

  @Transactional
  @Async("rolePersistenceExecutor")
  public void deleteOldRoles(RoleDeleteEvent event) {
    Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(event.getGuild().getIdLong());
    if (!serverEntity.isPresent()) {
      persistNewServer(event.getGuild());
    } else {
      repositoryContainer
          .getServerRoleRepository()
          .findByRoleGuidAndServerGuid(event.getRole().getIdLong(), event.getGuild().getIdLong())
          .ifPresent(serverRoleEntity -> repositoryContainer.getServerRoleRepository().delete(serverRoleEntity));

      loggerService.logServerRole(serverEntity.get(), event, ":no_entry: Deleted role");
    }
  }
}
