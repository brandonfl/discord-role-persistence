package com.brandonfl.discordrolepersistence.discordbot;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.discordbot.command.ChangeLogChannelCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.ChangePrefixCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.ChangeWelcomeBackChannelCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.GetRolesCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.HelpCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.LockRoleCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.PingCommand;
import com.brandonfl.discordrolepersistence.discordbot.command.UnlockRoleCommand;
import com.brandonfl.discordrolepersistence.discordbot.event.BotEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.MemberEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerRoleEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.UserJoinEvent;
import com.brandonfl.discordrolepersistence.executor.JoinExecutor;
import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscordBot {

  public final BotProperties botProperties;
  private final RepositoryContainer repositoryContainer;
  private final PersistExecutor persistExecutor;
  private final JoinExecutor joinExecutor;

  @Autowired
  public DiscordBot(BotProperties botProperties,
      RepositoryContainer repositoryContainer,
      PersistExecutor persistExecutor,
      JoinExecutor joinExecutor) {
    this.botProperties = botProperties;
    this.repositoryContainer = repositoryContainer;
    this.persistExecutor = persistExecutor;
    this.joinExecutor = joinExecutor;
  }

  @PostConstruct
  public void startBot() throws LoginException {
    EventWaiter eventWaiter = new EventWaiter();
    // start getting a bot account set up

    CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
    commandClientBuilder
        .setOwnerId(botProperties.getSetting().getOwnerId())
        .setEmojis("\u2705", "\u26A0\uFE0F", "\u274C")
        .addCommands(
            new ChangeLogChannelCommand(repositoryContainer),
            new ChangePrefixCommand(repositoryContainer),
            new ChangeWelcomeBackChannelCommand(repositoryContainer),
            new GetRolesCommand(repositoryContainer, eventWaiter),
            new HelpCommand(repositoryContainer, botProperties),
            new LockRoleCommand(repositoryContainer),
            new PingCommand(),
            new UnlockRoleCommand(repositoryContainer)
    );

    JDA jda = new JDABuilder(AccountType.BOT)
        // set the token
        .setToken(botProperties.getSetting().getToken())
        .setAutoReconnect(true)

        // add the listeners
        .addEventListeners(
            eventWaiter,
            commandClientBuilder.build(),
            new ServerEvent(repositoryContainer, persistExecutor),
            new MemberEvent(persistExecutor),
            new ServerRoleEvent(persistExecutor),
            new BotEvent(persistExecutor),
            new UserJoinEvent(joinExecutor))
        // start it up!
        .build();

    DiscordBotUtils.updateJDAStatus(jda, true);
  }
}
