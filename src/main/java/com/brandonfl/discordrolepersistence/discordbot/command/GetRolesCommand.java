package com.brandonfl.discordrolepersistence.discordbot.command;

import static com.brandonfl.discordrolepersistence.utils.DiscordBotUtils.getGenericPaginatorBuilder;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.menu.Paginator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@CommandInfo(
    name = "roles",
    description = "Get roles status of the current server."
)
@AllArgsConstructor
public class GetRolesCommand extends Command {

  private final RepositoryContainer repositoryContainer;
  private final EventWaiter eventWaiter;

  @Getter
  private final String name = "roles";

  @Override
  protected void execute(CommandEvent event) {
    if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
      ServerEntity serverEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong()).orElse(null);
      if (serverEntity != null) {
        final Set<Long> serverRoleBlacklistedIds = serverEntity
            .getRoleEntities()
            .parallelStream()
            .filter(ServerRoleEntity::isBlacklisted)
            .map(ServerRoleEntity::getRoleGuid)
            .collect(Collectors.toSet());
        final Member currentBotMember = event.getGuild().getSelfMember();
        List<String> rolesString = new ArrayList<>();
        for (Role role : event.getGuild().getRoles()) {
          if (role.isPublicRole()) {
            continue;
          } else if (role.isManaged()) {
            rolesString.add(":robot: " + role.getAsMention() + " (Cannot be assigned manually)");
          } else if (role.hasPermission(Permission.ADMINISTRATOR)) {
            rolesString.add(":no_entry: " + role.getAsMention() + " (Administrator role)");
          } else if (!serverRoleBlacklistedIds.isEmpty()
              && serverRoleBlacklistedIds.stream()
              .anyMatch(roleId -> roleId.equals(role.getIdLong()))) {
            rolesString.add(":lock: " + role.getAsMention() + " (Locked role)");
          } else if (DiscordBotUtils.getUpperRole(currentBotMember.getRoles()) < role
              .getPosition()) {
            rolesString.add(":warning: " + role.getAsMention()
                + " (bot too low in the hierarchy to give this role)");
          } else {
            rolesString.add(":white_check_mark:  " + role.getAsMention());
          }
        }

        Paginator.Builder paginatorBuilder = getGenericPaginatorBuilder(eventWaiter);
        paginatorBuilder.clearItems();
        rolesString.forEach(paginatorBuilder::addItems);
        paginatorBuilder
            .setText("Server roles")
            .build()
            .paginate(event.getChannel(), 1);
      } else {
        event.replyWarning("Current server not found");
      }
    } else {
      event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action")
          .queue();
    }
  }
}
