package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

@CommandInfo(
    name = "lock",
    description = "Prevent the role from being rollback."
)
@AllArgsConstructor
public class LockRoleCommand extends Command {

  private final RepositoryContainer repositoryContainer;

  @Getter
  private final String name = "lock";

  @Override
  protected void execute(CommandEvent event) {
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, getName())) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, getName())) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedRoles().size() == 1 || DiscordBotUtils.verifyCommandFormat( msg, getName() + "[\\s]+[0-9]+$")) {
            Role role;
            if (msg.getMentionedRoles().size() == 1) {
              role = msg.getMentionedRoles().get(0);
            } else {
              role = event.getGuild().getRoleById(event.getMessage().getContentRaw().split(" ")[1]);
            }

            if (role != null) {
              Optional<ServerRoleEntity> possibleServerRoleEntity = repositoryContainer
                  .getServerRoleRepository()
                  .findByRoleGuidAndRoleGuid(role.getIdLong(), event.getGuild().getIdLong());
              ServerRoleEntity serverRoleEntity;
              if (possibleServerRoleEntity.isPresent()) {
                serverRoleEntity = possibleServerRoleEntity.get();
                if (serverRoleEntity.isBlacklisted()) {
                  event.getChannel().sendMessage(":x: This role is already locked for future rollbacks").queue();
                  return;
                }
              } else {
                serverRoleEntity = new ServerRoleEntity();
                serverRoleEntity.setServerGuid(possibleServerEntity.get());
                serverRoleEntity.setRoleGuid(role.getIdLong());
              }

              serverRoleEntity.setBlacklisted(true);
              repositoryContainer.getServerRoleRepository().save(serverRoleEntity);
              event.getChannel().sendMessage(":white_check_mark: Role " + role.getName() + " is now locked for future rollbacks").queue();

              Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(event.getGuild(), possibleServerEntity.get());
              if (logChannel.isPresent()) {
                EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(event.getJDA());
                embedBuilder
                    .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                    .addField(":lock: Locked rollbacks for role", role.getName() + " (" + role.getId() + ")", true);

                logChannel.get().sendMessage(embedBuilder.build()).queue();
              }
            } else {
              event.getChannel().sendMessage(":x: This role id is invalid").queue();
            }
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one role").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }
}
