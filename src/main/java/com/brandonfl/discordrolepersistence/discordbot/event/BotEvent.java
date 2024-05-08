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
import com.brandonfl.discordrolepersistence.db.entity.ServerUserSavedRolesEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
public class BotEvent extends ListenerAdapter {

  private final BotProperties botProperties;
  private final RepositoryContainer repositoryContainer;

  @Override
  @Transactional
  public void onReady(@Nonnull ReadyEvent event) {
    log.info("Bot ready !");

    if (botProperties.getSetting().getPersistence().needToReloadPersistenceAtBotReload()) {
      log.info("Reloading...");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();

      persistGuilds(event.getJDA());

      stopWatch.stop();
      log.info("Reloaded in {}ms", stopWatch.getTotalTimeMillis());
    } else {
      log.warn("Reload is currently disabled. Role changes during bot downtime can be lost.");
      DiscordBotUtils.updateJDAStatus(event.getJDA(), false);
    }
  }

  @Override
  @Transactional
  public void onReconnect(@Nonnull ReconnectedEvent event) {
    log.warn("Bot reconnected !");

    if (botProperties.getSetting().getPersistence().needToReloadPersistenceAtBotReload()) {
      persistGuilds(event.getJDA());
    } else {
      log.warn("Reload is currently disabled. Role changes during bot downtime can be lost.");
      DiscordBotUtils.updateJDAStatus(event.getJDA(), false);
    }
  }

  private void persistGuilds(@Nonnull JDA jda) {
    DiscordBotUtils.updateJDAStatus(jda, true);
    try {
      for (Guild guild : jda.getGuilds()) {
        for (Member member : guild.getMembers()) {
          if (member.getUser().isBot() || member.getUser().isSystem()) {
            continue;
          }

          for (Role role : member.getRoles()) {
            ServerUserSavedRolesEntity serverUserSavedRolesEntity = new ServerUserSavedRolesEntity();
            serverUserSavedRolesEntity.setServerGuid(guild.getIdLong());
            serverUserSavedRolesEntity.setUserGuid(member.getIdLong());
            serverUserSavedRolesEntity.setRoleGuid(role.getIdLong());
            repositoryContainer.getServerUserSavedRolesRepository().save(serverUserSavedRolesEntity);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error while persisting guilds : " + e.getMessage(), e);
    }

    DiscordBotUtils.updateJDAStatus(jda, false);
  }
}
