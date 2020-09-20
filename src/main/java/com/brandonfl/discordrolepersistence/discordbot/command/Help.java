package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.executor.CommandExecutor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class Help extends ListenerAdapter {
  private final CommandExecutor commandExecutor;
  private final BotProperties botProperties;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    commandExecutor.getHelp(event, botProperties);
  }
}