package com.brandonfl.discordrolepersistence.discordbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PingCommand extends Command {

  public PingCommand() {
    this.name = "ping";
    this.help = "Get bot ping.";
    this.guildOnly = false;
    this.cooldown = 60;
  }

  @Override
  protected void execute(CommandEvent event) {
    long time = System.currentTimeMillis();
    event.getChannel().sendMessage("Pong!") /* => RestAction<Message> */
        .queue(response /* => Message */ -> {
          response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
        });
  }
}
