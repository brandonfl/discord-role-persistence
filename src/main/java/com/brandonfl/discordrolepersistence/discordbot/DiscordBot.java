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

package com.brandonfl.discordrolepersistence.discordbot;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.discordbot.command.ChangeLogChannelCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.ChangeWelcomeBackChannelCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.GetRolesCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.LockRoleCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.PingCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.UnlockRoleCommand;
import com.brandonfl.discordrolepersistence.discordbot.event.BotEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.MemberEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.RoleEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerRoleEvent;
import com.brandonfl.discordrolepersistence.service.BotService;
import com.brandonfl.discordrolepersistence.service.LoggerService;
import com.brandonfl.discordrolepersistence.service.ServerService;
import com.brandonfl.discordrolepersistence.service.UserService;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiscordBot {

  public final BotProperties botProperties;
  private final RepositoryContainer repositoryContainer;
  private final UserService userService;
  private final BotService botService;
  private final ServerService serverService;
  private final LoggerService loggerService;

  public static final String SUCCESS_EMOJI = "\u2705";
  public static final String WARNING_EMOJI = "\u26A0\uFE0F";
  public static final String ERROR_EMOJI = "\u274C";
  public static final String FORBIDDEN_EMOJI = ":octagonal_sign:";

  @PostConstruct
  public void startBot() throws LoginException {
    EventWaiter eventWaiter = new EventWaiter();

    CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
    commandClientBuilder
        .setOwnerId(botProperties.getSetting().getOwnerId())
        .setEmojis(SUCCESS_EMOJI, WARNING_EMOJI, ERROR_EMOJI)
        .addSlashCommands(
            new PingCommand(),
            new GetRolesCommand(repositoryContainer, eventWaiter),
            new LockRoleCommand(repositoryContainer),
            new UnlockRoleCommand(repositoryContainer),
            new ChangeLogChannelCommand(repositoryContainer),
            new ChangeWelcomeBackChannelCommand(repositoryContainer)
        );

    if (botProperties.getSetting().getGuidDevelopmentId() != null) {
      log.warn("Force guild active. This setting is for development only.");
      commandClientBuilder.forceGuildOnly(botProperties.getSetting().getGuidDevelopmentId());
    }

    JDA jda = JDABuilder.createDefault(botProperties.getSetting().getToken())
        .setAutoReconnect(true)
        .addEventListeners(
            eventWaiter,
            commandClientBuilder.build(),
            new ServerEvent(repositoryContainer, serverService),
            new RoleEvent(botProperties, userService, loggerService),
            new ServerRoleEvent(serverService),
            new BotEvent(botProperties, botService),
            new MemberEvent(userService))
        .build();

    DiscordBotUtils.updateJDAStatus(jda, false);
  }
}
