package com.brandonfl.discordrolepersistence.discordbot;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.discordbot.event.BotEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.CommandEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.MemberEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.ServerRoleEvent;
import com.brandonfl.discordrolepersistence.discordbot.event.UserJoinEvent;
import com.brandonfl.discordrolepersistence.executor.CommandExecutor;
import com.brandonfl.discordrolepersistence.executor.JoinExecutor;
import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
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
    new JDABuilder(AccountType.BOT)
        // set the token
        .setToken(botProperties.getSetting().getToken())

        // set the game for when the bot is loading
        .setStatus(OnlineStatus.ONLINE)
        .setActivity(Activity.playing("discord-role-persistence.com default prefix : drp!"))
        .setAutoReconnect(true)

        // add the listeners
        .addEventListeners(eventWaiter)
        .addEventListeners(new CommandEvent(new CommandExecutor(repositoryContainer, eventWaiter), botProperties))
        .addEventListeners(new ServerEvent(repositoryContainer, persistExecutor))
        .addEventListeners(new MemberEvent(persistExecutor))
        .addEventListeners(new ServerRoleEvent(persistExecutor))
        .addEventListeners(new BotEvent(persistExecutor))
        .addEventListeners(new UserJoinEvent(joinExecutor))

        // start it up!
        .build();
  }
}
