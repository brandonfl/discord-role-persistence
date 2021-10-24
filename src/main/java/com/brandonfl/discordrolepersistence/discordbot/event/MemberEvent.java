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

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.service.PersistenceService;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class MemberEvent extends ListenerAdapter {

  private final BotProperties botProperties;
  private final PersistenceService persistenceService;

  @Override
  public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
    persistenceService.logRoleUpdate(event, event.getMember(), event.getRoles(), ":white_check_mark: Added roles");

    if (botProperties.getSetting().getPersistence().needToPersistAtRoleChange()) {
      persistenceService.persistUser(event.getGuild(), event.getMember());
    }
  }

  @Override
  public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
    persistenceService.logRoleUpdate(event, event.getMember(), event.getRoles(), ":no_entry: Removed roles");

    if (botProperties.getSetting().getPersistence().needToPersistAtRoleChange()) {
      persistenceService.persistUser(event.getGuild(), event.getMember());
    }
  }
}
