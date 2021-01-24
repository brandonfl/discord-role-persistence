package com.brandonfl.discordrolepersistence.discordbot.command;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class UnlockRoleCommand extends Command {

  private final RepositoryContainer repositoryContainer;

  public UnlockRoleCommand(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;

    this.name = "unlock";
    this.help = "Allows the role to be rollback. By default, all the roles are unlock except admin roles.";
    this.arguments = "<@role OR roleId>";
  }

  @Override
  protected void execute(CommandEvent event) {
    Message msg = event.getMessage();
    if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
      if (msg.getMentionedRoles().size() == 1 || DiscordBotUtils.isArgAnId(event)) {
        Role role;
        if (msg.getMentionedRoles().size() == 1) {
          role = msg.getMentionedRoles().get(0);
        } else {
          try {
            role = event.getGuild().getRoleById(event.getArgs());
          } catch (Exception exception) {
            event.replyError("Invalid role id");
            return;
          }
        }

        if (role != null) {
          ServerEntity serverEntity = repositoryContainer.getServerRepository()
              .findByGuid(event.getGuild().getIdLong()).orElse(null);

          if (serverEntity != null) {
            Optional<ServerRoleEntity> possibleServerRoleEntity = repositoryContainer
                .getServerRoleRepository()
                .findByRoleGuidAndRoleGuid(role.getIdLong(), event.getGuild().getIdLong());
            ServerRoleEntity serverRoleEntity = null;
            if (possibleServerRoleEntity.isPresent()) {
              serverRoleEntity = possibleServerRoleEntity.get();
              if (!serverRoleEntity.isBlacklisted()) {
                event.replyError("This role is already unlocked for future rollbacks");
                return;
              }
            } else {
              serverRoleEntity = new ServerRoleEntity();
              serverRoleEntity.setServerGuid(serverEntity);
              serverRoleEntity.setRoleGuid(role.getIdLong());
            }
            serverRoleEntity.setBlacklisted(false);
            repositoryContainer.getServerRoleRepository().save(serverRoleEntity);
            event.replySuccess("Role " + role.getName() + " is now unlocked for future rollbacks");

            Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(event.getGuild(), serverEntity);
            if (logChannel.isPresent()) {
              EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(event.getJDA());
              embedBuilder
                  .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                  .addField(":unlock: Unlocked rollbacks for role", role.getName() + " (" + role.getId() + ")", true);

              logChannel.get().sendMessage(embedBuilder.build()).queue();
            }
          } else {
            event.replyWarning("Current server not found");
          }
        } else {
          event.replyError("This role id is invalid");
        }
      } else {
        event.replyError("Please provide one and exactly only one role");
      }
    } else {
      event.replyError("Only administrators can perform this action");
    }
  }
}
