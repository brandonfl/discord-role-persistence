/*
 * MIT License
 *
 * Copyright (c) 2021 Fontany--Legall Brandon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.brandonfl.discordrolepersistence.discordbot.command.slash;

import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.ERROR_EMOJI;
import static com.brandonfl.discordrolepersistence.utils.DiscordBotUtils.getGenericPaginatorBuilder;

import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.EmbedPaginator;
import com.jagrosh.jdautilities.menu.Paginator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.transaction.annotation.Transactional;

public class GetRolesCommand extends SlashCommand {

  private final RepositoryContainer repositoryContainer;
  private final EventWaiter eventWaiter;

  public GetRolesCommand(
      RepositoryContainer repositoryContainer,
      EventWaiter eventWaiter) {
    this.repositoryContainer = repositoryContainer;
    this.eventWaiter = eventWaiter;

    this.name = "roles";
    this.help = "Get roles status of the current server.";
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
  }

  @Override
  @Transactional(readOnly = true)
  public void execute(SlashCommandEvent event) {
    event.deferReply().setContent("Retrieving roles...").queue();

    if (event.getGuild() == null) {
      event
          .getHook()
          .editOriginalFormat("%s Current server not existing", ERROR_EMOJI)
          .queue();
      return;
    }

    final Set<Long> serverRoleBlacklistedIds = repositoryContainer.getServerRoleRepository()
        .findAllByServerGuidId(event.getGuild().getIdLong())
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
        .paginate(event.getHook().retrieveOriginal().complete(), 1);
  }
}
