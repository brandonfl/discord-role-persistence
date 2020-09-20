package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.executor.CommandExecutor;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class CommandEvent extends ListenerAdapter {

  private final CommandExecutor commandExecutor;
  private final BotProperties botProperties;

  @Override
  public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
    commandExecutor.executeCommand(event, botProperties);
  }
}
