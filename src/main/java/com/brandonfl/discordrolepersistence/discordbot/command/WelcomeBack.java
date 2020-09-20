package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.executor.CommandExecutor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class WelcomeBack extends ListenerAdapter {
  private final CommandExecutor commandExecutor;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    commandExecutor.changeWelcomeBackChannel(event);
  }

}
