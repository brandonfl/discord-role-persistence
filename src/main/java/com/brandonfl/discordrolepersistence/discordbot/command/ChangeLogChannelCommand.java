package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
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
    if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
      ServerEntity serverEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong()).orElse(null);
      if (serverEntity != null) {
        if (msg.getMentionedChannels().size() == 1) {
          if (msg.getMentionedChannels().get(0).canTalk()) {
            serverEntity.setLogChannel(msg.getMentionedChannels().get(0).getIdLong());
            repositoryContainer.getServerRepository().save(serverEntity);

            event.replySuccess("Log channel has been changed");
          } else {
            event.replyError("It seems that the bot dont have talk access to this channel");
          }
        } else if (event.getArgs().equals("disable")) {
          serverEntity.setLogChannel(null);
          repositoryContainer.getServerRepository().save(serverEntity);

          event.replySuccess("Log channel has been disabled");
        } else {
          event.replyError("Please provide one and exactly only one channel");
        }
      } else {
        event.replyWarning("Current server not found");
      }
    } else {
      event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action")
          .queue();
    }
  }
}
