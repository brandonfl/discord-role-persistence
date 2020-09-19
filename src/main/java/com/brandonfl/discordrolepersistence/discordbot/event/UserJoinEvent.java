package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserJoinEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;

  public UserJoinEvent(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Override
  public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
    Optional<ServerUserEntity> serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(event.getMember().getIdLong(), event.getGuild().getIdLong());

    if (serverUserEntity.isPresent()) {
      Set<Role> rolesToAddToUser = serverUserEntity
          .get()
          .getRoleEntities()
          .stream()
          .filter(serverRoleEntity -> !serverRoleEntity.isBlacklisted())
          .map(serverRoleEntity -> event.getGuild().getRoleById(serverRoleEntity.getRoleGuid()))
          .filter(role -> role != null && !role.hasPermission(Permission.ADMINISTRATOR))
          .collect(Collectors.toSet());

      for (Role role : rolesToAddToUser) {
        try {
          event.getGuild().addRoleToMember(event.getMember(), role).complete();
        } catch (Exception e) {
          System.out.println("NOOOOOO");
        }

        System.out.println("role " + role);
        System.out.println("role " + role.getName());
        System.out.println("role.isHoisted() " + role.isHoisted());
        System.out.println("role.isManaged() " + role.isManaged());
        System.out.println("role.isPublicRole() " + role.isPublicRole());
        System.out.println("role.getPermissionsRaw() " + role.getPermissionsRaw());

      }




    }

    super.onGuildMemberJoin(event);
  }
}
