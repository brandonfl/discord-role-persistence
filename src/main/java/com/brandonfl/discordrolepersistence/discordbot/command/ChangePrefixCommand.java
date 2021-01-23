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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

@CommandInfo(
    name = "prefix",
    description = "Change bot prefix."
)
@AllArgsConstructor
public class ChangePrefixCommand extends Command {

  private final RepositoryContainer repositoryContainer;

  @Getter
  private final String name = "prefix";

  @Override
  protected void execute(CommandEvent event) {
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, getName())) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, getName())) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getContentRaw().split(" ").length == 2) {
            final String newPrefix = msg.getContentRaw().split(" ")[1].trim();

            ServerEntity serverEntity = possibleServerEntity.get();
            serverEntity.setCommandPrefix(newPrefix);
            repositoryContainer.getServerRepository().save(serverEntity);

            event.getChannel().sendMessage(":white_check_mark: The command prefix is now `" + newPrefix + "`").queue();

            Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(event.getGuild(), possibleServerEntity.get());
            if (logChannel.isPresent()) {
              EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(event.getJDA());
              embedBuilder
                  .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                  .setTitle(":round_pushpin: Changed command prefix to `" + newPrefix + "`");

              logChannel.get().sendMessage(embedBuilder.build()).queue();
            }
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one prefix").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }
}
