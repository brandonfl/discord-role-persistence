package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;

  public ServerEvent(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Override
  public void onGuildJoin(@Nonnull GuildJoinEvent event) {
    if (!repositoryContainer.getServerRepository()
        .findById(event.getGuild().getIdLong()).isPresent()) {

      ServerEntity serverEntity = new ServerEntity();
      serverEntity.setGuid(event.getGuild().getIdLong());

      serverEntity = repositoryContainer.getServerRepository().save(serverEntity);

      Set<ServerRoleEntity> roles = new HashSet<>();
      for (Role role : event.getGuild().getRoles()) {
        ServerRoleEntity serverRoleEntity = new ServerRoleEntity();
        serverRoleEntity.setServerGuid(serverEntity);
        serverEntity.setGuid(role.getGuild().getIdLong());

        roles.add(serverRoleEntity);
      }
      roles = new HashSet<>(repositoryContainer.getServerRoleRepository().saveAll(roles));

      Set<ServerUserEntity> users = new HashSet<>();
      for (Member member : event.getGuild().getMembers()) {
        if (!member.isFake() && !member.getUser().isBot()) {
          Set<Long> memberRoleIds = member.getRoles().stream().map(Role::getIdLong).collect(Collectors.toSet());

          ServerUserEntity serverUserEntity = new ServerUserEntity();
          serverUserEntity.setServerGuid(serverEntity);
          serverUserEntity.setUserGuid(member.getIdLong());
          serverUserEntity.setRoleEntities(roles.stream().filter(role -> memberRoleIds
              .contains(role.getRoleGuid()))
              .collect(Collectors.toSet()));

          users.add(serverUserEntity);
        }
      }
      repositoryContainer.getServerUserRepository().saveAll(users);
    }
  }

  @Override
  public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
    repositoryContainer.getServerRepository()
        .findById(event.getGuild().getIdLong())
        .ifPresent(entity -> repositoryContainer.getServerRepository().delete(entity));
  }
}
