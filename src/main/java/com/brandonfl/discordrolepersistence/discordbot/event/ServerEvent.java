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
import com.brandonfl.discordrolepersistence.db.entity.ServerUserSavedRolesEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class ServerEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;

  @Override
  public void onGuildJoin(@Nonnull GuildJoinEvent event) {
    ServerEntity serverEntity = repositoryContainer.getServerRepository()
        .findById(event.getGuild().getIdLong())
        .orElse(new ServerEntity());

    serverEntity.setGuid(event.getGuild().getIdLong());
    repositoryContainer.getServerRepository().save(serverEntity);

    for (Member member : event.getGuild().getMembers()) {
      for (Role role : member.getRoles()) {
        ServerUserSavedRolesEntity serverUserSavedRolesEntity = new ServerUserSavedRolesEntity();
        serverUserSavedRolesEntity.setServerGuid(event.getGuild().getIdLong());
        serverUserSavedRolesEntity.setUserGuid(member.getIdLong());
        serverUserSavedRolesEntity.setRoleGuid(role.getIdLong());
        repositoryContainer.getServerUserSavedRolesRepository().save(serverUserSavedRolesEntity);
      }
    }
  }

  @Override
  public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
    repositoryContainer.getServerRepository()
        .deleteAllByGuid(event.getGuild().getIdLong());

    repositoryContainer.getServerUserSavedRolesRepository()
        .deleteAllByServerGuid(event.getGuild().getIdLong());

    repositoryContainer.getServerRoleBlacklistRepository()
        .deleteAllByServerGuid(event.getGuild().getIdLong());

    repositoryContainer.getServerRoleAdminEnableBackupRepository()
        .deleteAllByServerGuid(event.getGuild().getIdLong());
  }
}
