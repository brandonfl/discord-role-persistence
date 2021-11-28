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
import com.brandonfl.discordrolepersistence.service.BotService;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class BotEvent extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(BotEvent.class);
  private final BotProperties botProperties;
  private final BotService botService;

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    logger.info("Bot ready !");

    if (botProperties.getSetting().getPersistence().needToReloadPersistenceAtBotReload()) {
      botService.persistGuilds(event.getJDA());
    } else {
      logger.warn("Reload is currently disabled. Role changes during bot downtime can be lost.");
    }
  }

  @Override
  public void onReconnect(@Nonnull ReconnectedEvent event) {
    logger.warn("Bot reconnected !");

    if (botProperties.getSetting().getPersistence().needToReloadPersistenceAtBotReload()) {
      botService.persistGuilds(event.getJDA());
    } else {
      logger.warn("Reload is currently disabled. Role changes during bot downtime can be lost.");
    }
  }
}
