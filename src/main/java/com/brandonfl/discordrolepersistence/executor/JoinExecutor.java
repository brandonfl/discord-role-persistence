package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class JoinExecutor {

  private final RepositoryContainer repositoryContainer;

  @Autowired
  public JoinExecutor(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Transactional
  @Async("asyncPersistExecutor")
  public void backupRoleOfMember(@Nonnull GuildMemberJoinEvent joinEvent) {
    Optional<ServerUserEntity> serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(joinEvent.getMember().getIdLong(), joinEvent.getGuild().getIdLong());

    if (serverUserEntity.isPresent()) {
      Set<Role> rolesToAddToUser = serverUserEntity
          .get()
          .getRoleEntities()
          .stream()
          .filter(serverRoleEntity -> !serverRoleEntity.isBlacklisted())
          .map(serverRoleEntity -> joinEvent.getGuild().getRoleById(serverRoleEntity.getRoleGuid()))
          .filter(role -> role != null && !role.hasPermission(Permission.ADMINISTRATOR))
          .collect(Collectors.toSet());

      for (Role role : rolesToAddToUser) {
        try {
          joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).complete();
        } catch (Exception e) {
          System.out.println("NOOOOOO");
        }
      }
    }
  }

}
