package com.brandonfl.discordrolepersistence.discordbot;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.discordbot.command.Help;
import com.brandonfl.discordrolepersistence.discordbot.command.Log;
import com.brandonfl.discordrolepersistence.discordbot.command.PingPong;
import com.brandonfl.discordrolepersistence.discordbot.event.BotEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.MemberEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerRoleEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.UserJoinEvent;
import com.brandonfl.discordrolepersistence.executor.CommandExecutor;
import com.brandonfl.discordrolepersistence.executor.JoinExecutor;
import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscordBot {

  public static final String PREFIX = "!";
  public final BotProperties botProperties;
  private final RepositoryContainer repositoryContainer;
  private final PersistExecutor persistExecutor;
  private final JoinExecutor joinExecutor;
  private final CommandExecutor commandExecutor;

  @Autowired
  public DiscordBot(BotProperties botProperties,
      RepositoryContainer repositoryContainer,
      PersistExecutor persistExecutor,
      JoinExecutor joinExecutor,
      CommandExecutor commandExecutor) {
    this.botProperties = botProperties;
    this.repositoryContainer = repositoryContainer;
    this.persistExecutor = persistExecutor;
    this.joinExecutor = joinExecutor;
    this.commandExecutor = commandExecutor;
  }

  @PostConstruct
  public void startBot() throws LoginException {
    // start getting a bot account set up
    new JDABuilder(AccountType.BOT)
        // set the token
        .setToken(botProperties.getSetting().getToken())

        // set the game for when the bot is loading
        .setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.playing(PREFIX + "help"))
        .setAutoReconnect(true)

        // add the listeners
        .addEventListeners(new PingPong(commandExecutor))
        .addEventListeners(new Help(commandExecutor, botProperties))
        .addEventListeners(new Log(commandExecutor))

        .addEventListeners(new ServerEvent(repositoryContainer, persistExecutor))
        .addEventListeners(new MemberEvent(persistExecutor))
        .addEventListeners(new ServerRoleEvent(persistExecutor))
        .addEventListeners(new BotEvent(persistExecutor))
        .addEventListeners(new UserJoinEvent(joinExecutor))
        // start it up!
        .build();
  }
}
