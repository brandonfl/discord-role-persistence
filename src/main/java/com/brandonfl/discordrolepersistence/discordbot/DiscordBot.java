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
import com.brandonfl.discordrolepersistence.discordbot.command.slash.ChangeLogChannelCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.slash.ChangeWelcomeBackChannelCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.slash.CleanSavedRolesCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.slash.DisableRollbackRoleCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.slash.EnableRollbackRoleCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.slash.GetRolesCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.slash.PingCommand;
import com.brandonfl.discordrolepersistence.discordbot.event.BotEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.MemberEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.RoleEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerRoleEvent;
import com.brandonfl.discordrolepersistence.service.LoggerService;
import com.brandonfl.discordrolepersistence.service.UserService;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jakarta.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiscordBot {

  public final BotProperties botProperties;
  private final RepositoryContainer repositoryContainer;
  private final UserService userService;
  private final LoggerService loggerService;
  private final DiscordBotUtils discordBotUtils;

  public static final String SUCCESS_EMOJI = "\u2705";
  public static final String WARNING_EMOJI = "\u26A0\uFE0F";
  public static final String ERROR_EMOJI = "\u274C";
  public static final String FORBIDDEN_EMOJI = ":octagonal_sign:";

  public static final Activity DEFAULT_ACTIVITY = Activity.playing("use / | discord-role-persistence.com");

  @PostConstruct
  public void startBot() throws LoginException {
    EventWaiter eventWaiter = new EventWaiter();

    CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
    commandClientBuilder
        .setOwnerId(botProperties.getSetting().getOwnerId())
        .setEmojis(SUCCESS_EMOJI, WARNING_EMOJI, ERROR_EMOJI)
        .useHelpBuilder(true)
        .setPrefix("/")
        .setAlternativePrefix("@mention")
        .addSlashCommands(
            new PingCommand(),
            new GetRolesCommand(repositoryContainer, eventWaiter),
            new DisableRollbackRoleCommand(repositoryContainer, discordBotUtils),
            new EnableRollbackRoleCommand(repositoryContainer, discordBotUtils),
            new ChangeLogChannelCommand(repositoryContainer),
            new ChangeWelcomeBackChannelCommand(repositoryContainer),
            new CleanSavedRolesCommand(repositoryContainer)
        );

    if (botProperties.getSetting().getGuidDevelopmentId() != null) {
      log.warn("Force guild active. This setting is for development only.");
      commandClientBuilder.forceGuildOnly(String.valueOf(botProperties.getSetting().getGuidDevelopmentId()));
    }

    DefaultShardManagerBuilder
        .create(
            GatewayIntent.GUILD_MEMBERS, // Used to get members join/leave/roles
            GatewayIntent.GUILD_MESSAGES, // Used to get messages as @mention
            GatewayIntent.GUILD_MESSAGE_REACTIONS, // Used for the paginator
            GatewayIntent.DIRECT_MESSAGES) // Used to allow some commands by direct messages
        .setToken(botProperties.getSetting().getToken())
        .setShardsTotal(botProperties.getSetting().getShardsTotal())
        .setAutoReconnect(true)
        .addEventListeners(
            eventWaiter,
            commandClientBuilder.build(),
            new ServerEvent(repositoryContainer),
            new RoleEvent(botProperties, userService, loggerService),
            new ServerRoleEvent(repositoryContainer, loggerService),
            new BotEvent(botProperties, repositoryContainer),
            new MemberEvent(userService))
        .setActivity(DEFAULT_ACTIVITY)
        .build();
  }
}
