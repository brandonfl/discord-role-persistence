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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@CommandInfo(
    name = "log",
    description = "Change or disable logger channel."
)
@AllArgsConstructor
public class ChangeLogChannelCommand extends Command {

  private final RepositoryContainer repositoryContainer;

  @Getter
  private final String name = "log";

  @Override
  protected void execute(CommandEvent event) {
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, getName())) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, getName())) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedChannels().size() == 1) {
            if (msg.getMentionedChannels().get(0).canTalk()) {
              ServerEntity serverEntityToUpdate = possibleServerEntity.get();
              serverEntityToUpdate.setLogChannel(msg.getMentionedChannels().get(0).getIdLong());
              repositoryContainer.getServerRepository().save(serverEntityToUpdate);

              event.getChannel().sendMessage(":white_check_mark: Log channel has been changed").queue();
            } else {
              event.getChannel().sendMessage(":x: It seems that the bot dont have talk access to this channel").queue();
            }
          } else if (DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, getName() + " disable")){
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setLogChannel(null);
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getChannel().sendMessage(":white_check_mark: Log channel has been disabled").queue();
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one channel ").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }
}
