package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PersistExecutor {

  private final RepositoryContainer repositoryContainer;

  @Autowired
  public PersistExecutor(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Transactional
  @Async("asyncPersistExecutor")
  public void persistNewServer(@Nonnull GuildJoinEvent event) {
    ServerEntity serverEntity = new ServerEntity();
    serverEntity.setGuid(event.getGuild().getIdLong());

    serverEntity = repositoryContainer.getServerRepository().save(serverEntity);

    Set<ServerRoleEntity> roles = new HashSet<>();
    for (Role role : event.getGuild().getRoles()) {
      ServerRoleEntity serverRoleEntity = new ServerRoleEntity();
      serverRoleEntity.setServerGuid(serverEntity);
      serverRoleEntity.setRoleGuid(role.getIdLong());

      roles.add(serverRoleEntity);
    }
    List<ServerRoleEntity> createdRoles = repositoryContainer.getServerRoleRepository().saveAll(roles);

    Set<ServerUserEntity> users = new HashSet<>();
    for (Member member : event.getGuild().getMembers()) {
      if (!member.isFake() && !member.getUser().isBot()) {
        Set<Long> memberRoleIds = member.getRoles().stream().map(Role::getIdLong).collect(
            Collectors.toSet());

        ServerUserEntity serverUserEntity = new ServerUserEntity();
        serverUserEntity.setServerGuid(serverEntity);
        serverUserEntity.setUserGuid(member.getIdLong());
        serverUserEntity.setRoleEntities(createdRoles.stream().filter(role -> memberRoleIds
            .contains(role.getRoleGuid()))
            .collect(Collectors.toSet()));

        users.add(serverUserEntity);
      }
    }
    repositoryContainer.getServerUserRepository().saveAll(users);
  }

}
