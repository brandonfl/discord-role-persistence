package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CommandExecutor {

  private final RepositoryContainer repositoryContainer;

  @Autowired
  public CommandExecutor(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Async("asyncCommandExecutor")
  public void getHelp(MessageReceivedEvent event, BotProperties botProperties) {
    final String command = "help";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(event.getGuild().getIdLong());
      if (serverEntity.isPresent() && DiscordBotUtils.verifyCommand(serverEntity.get(), msg, command)) {
        EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed();
        embedBuilder
            .addField("Commands",
                "ping\n"
                , true)
            .addField("Description",
                "Get discord bot ping\n"
                , true)
            .addField("Version", botProperties.getSetting().getVersion(), false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
      }
    }
  }

  @Async("asyncCommandExecutor")
  public void getPing(MessageReceivedEvent event) {
    final String command = "ping";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (serverEntity.isPresent() && DiscordBotUtils.verifyCommand(serverEntity.get(), msg, command)) {
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
