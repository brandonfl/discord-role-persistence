package com.brandonfl.discordrolepersistence.discordbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@CommandInfo(
    name = "ping",
    description = "Get bot ping."
)
@AllArgsConstructor
public class PingCommand extends Command {

  @Getter
  private final String name = "ping";

  @Override
  protected void execute(CommandEvent event) {
    long time = System.currentTimeMillis();
    event.getChannel().sendMessage("Pong!") /* => RestAction<Message> */
        .queue(response /* => Message */ -> {
          response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
        });
  }
}
