package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import java.util.Optional;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

@CommandInfo(
    name = "help",
    description = "Get bot commands list."
)
@AllArgsConstructor
public class HelpCommand extends Command {

  private final RepositoryContainer repositoryContainer;
  private final BotProperties botProperties;

  @Override
  public String getName() {
    return "help";
  }

  @Override
  protected void execute(CommandEvent event) {
    System.out.println(getName());
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, getName())) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(event.getGuild().getIdLong());
      if (serverEntity.isPresent() && DiscordBotUtils.verifyCommand(serverEntity.get(), msg, getName())) {
        EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(event.getJDA());
        embedBuilder
            .setAuthor("Discord Role Persistence commands")
            .addField("Commands",
                "`ping`\n"
                    + "`prefix prefix`\n"
                    + "`log #Channel`\n"
                    + "`log disable`\n"
                    + "`welcome-back #Channel`\n"
                    + "`welcome-back disable`\n"
                    + "`roles`\n"
                    + "`lock #Role`\n"
                    + "`lock roleId`\n"
                    + "`unlock #Role`\n"
                    + "`unlock roleId`"
                , true)
            .addField("Description",
                "`Get discord bot ping`\n"
                    + "`Change bot prefix`\n"
                    + "`Change logger channel`\n"
                    + "`Disable logger channel`\n"
                    + "`Change welcome back channel`\n"
                    + "`Disable welcome back channel`\n"
                    + "`Get roles status of the current server`\n"
                    + "`Preventing the role from being rollback`\n"
                    + "`Preventing the role with id from being rollback`\n"
                    + "`Allows the role to be rollback`\n"
                    + "`Allows the role with id to be rollback`\n"
                , true)
            .addField("Version", botProperties.getSetting().getVersion(), false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
      }
    }
  }
}
