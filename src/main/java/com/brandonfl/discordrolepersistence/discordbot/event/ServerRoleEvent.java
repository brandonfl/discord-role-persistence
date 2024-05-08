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

import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.service.LoggerService;
import jakarta.transaction.Transactional;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class ServerRoleEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;
  private final LoggerService loggerService;

  @Override
  public void onRoleCreate(@Nonnull RoleCreateEvent event) {
    logRoleEvent(event, ":white_check_mark: Created new role");
  }

  @Override
  @Transactional
  public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
    repositoryContainer.getServerUserSavedRolesRepository()
        .deleteAllByServerGuidAndAndRoleGuid(event.getGuild().getIdLong(), event.getRole().getIdLong());

    repositoryContainer.getServerRoleBlacklistRepository()
        .deleteAllByServerGuidAndRoleGuid(event.getGuild().getIdLong(), event.getRole().getIdLong());

    repositoryContainer.getServerRoleAdminEnableBackupRepository()
        .deleteAllByServerGuidAndRoleGuid(event.getGuild().getIdLong(), event.getRole().getIdLong());

    logRoleEvent(event, ":no_entry: Deleted role");
  }

  private void logRoleEvent(GenericRoleEvent event, String message) {
    repositoryContainer.getServerRepository()
        .findByGuid(event.getGuild().getIdLong())
        .ifPresent(serverEntity -> loggerService
            .logServerRole(serverEntity, event, message));
  }
}
