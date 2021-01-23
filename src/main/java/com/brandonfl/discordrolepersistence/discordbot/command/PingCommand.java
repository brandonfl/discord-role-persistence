package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@CommandInfo(
    name = "ping",
    description = "Get bot ping."
)
@AllArgsConstructor
public class PingCommand extends Command {

  private final RepositoryContainer repositoryContainer;

  @Getter
  private final String name = "ping";

  @Override
  protected void execute(CommandEvent event) {
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, getName())) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (serverEntity.isPresent() && DiscordBotUtils.verifyCommand(serverEntity.get(), msg, getName())) {
        MessageChannel channel = event.getChannel();
        long time = System.currentTimeMillis();
        channel.sendMessage("Pong!") /* => RestAction<Message> */
            .queue(response /* => Message */ -> {
              response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
            });
      }
    }
  }
}
