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

import com.brandonfl.discordrolepersistence.db.entity.ServerUserSavedRolesEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

  private final RepositoryContainer repositoryContainer;
  private final LoggerService loggerService;

  @Async("userPersistenceExecutor")
  @Transactional
  public void persistUser(@Nonnull Guild guild, @Nonnull Member member) {
    repositoryContainer.getServerUserSavedRolesRepository()
        .deleteAllByServerGuidAndAndUserGuid(guild.getIdLong(), member.getIdLong());

    for (Role role : member.getRoles()) {
      ServerUserSavedRolesEntity serverUserSavedRolesEntity = new ServerUserSavedRolesEntity();
      serverUserSavedRolesEntity.setRoleGuid(role.getIdLong());
      serverUserSavedRolesEntity.setUserGuid(member.getIdLong());
      serverUserSavedRolesEntity.setServerGuid(guild.getIdLong());
      repositoryContainer.getServerUserSavedRolesRepository().save(serverUserSavedRolesEntity);
    }
  }

  @Async("userPersistenceExecutor")
  public void backupRoles(@Nonnull GuildMemberJoinEvent joinEvent) {
    final List<Long> userPreviousRoles = repositoryContainer
        .getServerUserSavedRolesRepository()
        .findAllNonBacklistedRolesByServerGuidAndUserGuid(joinEvent.getGuild().getIdLong(), joinEvent.getUser().getIdLong());

    final List<Long> adminRollbackRoles = repositoryContainer
        .getServerRoleAdminEnableBackupRepository()
        .getRoleAdminEnableBackupByServerGuid(joinEvent.getGuild().getIdLong());

    final Set<Role> rolesAddedToUser = new HashSet<>();
    final int botUpperRole = DiscordBotUtils.getUpperRole(joinEvent.getGuild().getSelfMember().getRoles());

    userPreviousRoles.parallelStream().forEach(
        roleGuid -> {
          final Role role = joinEvent.getGuild().getRoleById(roleGuid);

          if (role != null
              && (!role.hasPermission(Permission.ADMINISTRATOR) || adminRollbackRoles.contains(roleGuid))
              && (botUpperRole > role.getPosition())) {

            joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).queue();
            rolesAddedToUser.add(role);
          }
        }
    );

    if (!rolesAddedToUser.isEmpty()) {
      loggerService.logRolesGivedBack(joinEvent, joinEvent.getGuild().getIdLong(), rolesAddedToUser);
    }
  }
}
