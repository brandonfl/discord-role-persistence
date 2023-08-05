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
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotService {

  private final RepositoryContainer repositoryContainer;
  private final EntityManager entityManager;
  private final ServerService serverService;

  @Transactional
  public void persistGuilds(@Nonnull JDA jda) {
    DiscordBotUtils.updateJDAStatus(jda, true);
    for (Guild guild : jda.getGuilds()) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(guild.getIdLong());
      if (!serverEntity.isPresent()) {
        serverService.persistNewServer(guild);
      } else {
        deleteOldRoles(guild, serverEntity.get());
        createNewRoles(guild, serverEntity.get());
        createNewMembers(guild, serverEntity.get());

        entityManager.flush();
        entityManager.clear();
        updateMemberRoles(guild, repositoryContainer.getServerRepository().getOne(serverEntity.get().getGuid()));
      }
    }
    DiscordBotUtils.updateJDAStatus(jda, false);
  }

  private void deleteOldRoles(Guild guild, ServerEntity serverEntity) {
    Set<Long> roleGuids = guild.getRoles().stream().map(Role::getIdLong).collect(Collectors.toSet());
    Set<ServerRoleEntity> serverRoles = serverEntity.getRoleEntities();

    repositoryContainer.getServerRoleRepository().deleteAll(
        serverRoles
            .stream()
            .filter(serverRoleEntity -> !roleGuids.contains(serverRoleEntity.getRoleGuid()))
            .collect(Collectors.toSet()));
  }

  private void createNewRoles(Guild guild, ServerEntity serverEntity) {
    Set<Long> roleGuids = guild.getRoles().stream().map(Role::getIdLong).collect(Collectors.toSet());
    Set<Long> alreadyCreatedRoles = serverEntity.getRoleEntities().stream().map(ServerRoleEntity::getRoleGuid).collect(Collectors.toSet());

    Set<ServerRoleEntity> serverRoleEntitiesToCreate = new HashSet<>();
    for (Long roleNotStored : roleGuids.stream().filter(guid -> !alreadyCreatedRoles.contains(guid)).collect(Collectors.toSet())) {
      ServerRoleEntity serverRoleEntity = new ServerRoleEntity();
      serverRoleEntity.setRoleGuid(roleNotStored);
      serverRoleEntity.setServerGuid(serverEntity);

      serverRoleEntitiesToCreate.add(serverRoleEntity);
    }

    repositoryContainer.getServerRoleRepository().saveAll(serverRoleEntitiesToCreate);
  }

  private void createNewMembers(Guild guild, ServerEntity serverEntity) {
    Set<Long> memberGuids = guild.getMembers()
        .stream()
        .filter(member -> !member.getUser().isBot() && !member.getUser().isSystem())
        .map(Member::getIdLong)
        .collect(Collectors.toSet());

    Set<Long> alreadyCreatedMembers = serverEntity.getUserEntities()
        .stream()
        .map(ServerUserEntity::getUserGuid)
        .collect(Collectors.toSet());

    Set<ServerUserEntity> serverUserEntitiesToCreate = new HashSet<>();
    for (Long memberNotCreated : memberGuids.stream().filter(guid -> !alreadyCreatedMembers.contains(guid)).collect(Collectors.toSet())) {
      ServerUserEntity serverUserEntity = new ServerUserEntity();
      serverUserEntity.setUserGuid(memberNotCreated);
      serverUserEntity.setServerGuid(serverEntity);
    }

    repositoryContainer.getServerUserRepository().saveAll(serverUserEntitiesToCreate);
  }

  private void updateMemberRoles(Guild guild, ServerEntity serverEntity) {
    Set<ServerRoleEntity> roleToUpdate = new HashSet<>();
    for (ServerRoleEntity serverRoleEntity : serverEntity.getRoleEntities()) {
      Role role = guild.getRoleById(serverRoleEntity.getRoleGuid());
      if (role != null) {
        Set<Long> membersOfRoleGuids = guild
            .getMembersWithRoles(role)
            .stream()
            .map(Member::getIdLong)
            .collect(Collectors.toSet());

        serverRoleEntity
            .setUserEntities(serverEntity.getUserEntities()
                .stream()
                .filter(serverUserEntity -> membersOfRoleGuids.contains(serverUserEntity.getUserGuid()))
                .collect(Collectors.toSet()));

        roleToUpdate.add(serverRoleEntity);
      }
    }

    repositoryContainer.getServerRoleRepository().saveAll(roleToUpdate);
  }
}
