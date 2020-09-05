package com.brandonfl.demobot.discordbot;

import com.brandonfl.demobot.config.BotProperties;
import com.brandonfl.demobot.db.repository.RepositoryContainer;
import com.brandonfl.demobot.discordbot.command.Help;
import com.brandonfl.demobot.discordbot.command.PingPong;
import com.brandonfl.demobot.discordbot.event.UserCounter;
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

  @Autowired
  public DiscordBot(BotProperties botProperties,
      RepositoryContainer repositoryContainer) {
    this.botProperties = botProperties;
    this.repositoryContainer = repositoryContainer;
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
        .addEventListeners(new PingPong())
        .addEventListeners(new UserCounter(repositoryContainer))
        .addEventListeners(new Help(botProperties))
        .addEventListeners()
        // start it up!
        .build();
  }
}
