package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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

  @Async("asyncCommandExecutor")
  public void changeLogChannel(MessageReceivedEvent event) {
    final String command = "log";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedChannels().size() == 1) {
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setLogChannel(msg.getMentionedChannels().get(0).getIdLong());
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getTextChannel().sendMessage(":white_check_mark: Log channel has been changed").queue();
          } else if (DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command + " disable")){
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setLogChannel(null);
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getTextChannel().sendMessage(":white_check_mark: Log channel has been disabled").queue();
          } else {
            event.getTextChannel().sendMessage(":x: Please provide one and exactly only one channel ").queue();
          }
        } else {
          event.getTextChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

  @Async("asyncCommandExecutor")
  public void changeWelcomeBackChannel(MessageReceivedEvent event) {
    final String command = "welcome-back";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedChannels().size() == 1) {
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setWelcomeBackChannel(msg.getMentionedChannels().get(0).getIdLong());
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getTextChannel().sendMessage(":white_check_mark: Welcome back channel has been changed").queue();
          } else if (DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command + " disable")){
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setWelcomeBackChannel(null);
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getTextChannel().sendMessage(":white_check_mark: Welcome back channel has been disabled").queue();
          } else {
            event.getTextChannel().sendMessage(":x: Please provide one and exactly only one channel ").queue();
          }
        } else {
          event.getTextChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

}
