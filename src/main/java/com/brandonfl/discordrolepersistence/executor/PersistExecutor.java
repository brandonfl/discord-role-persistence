package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
  public void persistNewServer(@Nonnull Guild guild) {
    ServerEntity serverEntity = new ServerEntity();
    serverEntity.setGuid(guild.getIdLong());

    serverEntity = repositoryContainer.getServerRepository().save(serverEntity);

    Set<ServerRoleEntity> roles = new HashSet<>();
    for (Role role : guild.getRoles()) {
      ServerRoleEntity serverRoleEntity = new ServerRoleEntity();
      serverRoleEntity.setServerGuid(serverEntity);
      serverRoleEntity.setRoleGuid(role.getIdLong());

      roles.add(serverRoleEntity);
    }
    List<ServerRoleEntity> createdRoles = repositoryContainer.getServerRoleRepository().saveAll(roles);

    Set<ServerUserEntity> users = new HashSet<>();
    for (Member member : guild.getMembers()) {
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

  @Transactional
  @Async("asyncPersistExecutor")
  public void persistRoleUpdateToUser(@Nonnull Guild guild, @Nonnull Member member) {
    Optional<ServerUserEntity> serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(member.getIdLong(), guild.getIdLong());

    if (!serverUserEntity.isPresent()) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(guild.getIdLong());
      if (!serverEntity.isPresent()) {
        persistNewServer(guild);
        return;
      } else {
        ServerUserEntity serverUserEntityToCreate = new ServerUserEntity();
        serverUserEntityToCreate.setServerGuid(serverEntity.get());
        serverUserEntityToCreate.setUserGuid(member.getIdLong());

        serverUserEntity = Optional.of(repositoryContainer.getServerUserRepository().save(serverUserEntityToCreate));

      }
    }

    List<Long> memberRoleIds = member.getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());

    ServerUserEntity userEntity = serverUserEntity.get();
    userEntity.setRoleEntities(userEntity
        .getServerGuid()
        .getRoleEntities()
        .stream()
        .filter(serverRoleEntity -> memberRoleIds.contains(serverRoleEntity.getRoleGuid()))
        .collect(Collectors.toSet()));

    repositoryContainer.getServerUserRepository().save(userEntity);
  }

  @Transactional
  @Async("asyncPersistExecutor")
  public void persistRoleUpdate(@Nonnull Guild guild) {
    Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(guild.getIdLong());
    if (!serverEntity.isPresent()) {
      persistNewServer(guild);
    } else {
      Set<Long> roleGuids = guild.getRoles().stream().map(Role::getIdLong).collect(Collectors.toSet());

      Set<ServerRoleEntity> serverRoles = serverEntity.get().getRoleEntities();

      repositoryContainer.getServerRoleRepository().deleteAll(
          serverRoles
              .stream()
              .filter(serverRoleEntity -> !roleGuids.contains(serverRoleEntity.getRoleGuid()))
              .collect(Collectors.toSet()));

      Set<Long> alreadyCreatedRoles = serverRoles.stream().map(ServerRoleEntity::getRoleGuid).collect(Collectors.toSet());
      for (Long roleNotStored : roleGuids.stream().filter(guid -> !alreadyCreatedRoles.contains(guid)).collect(Collectors.toSet())) {
        ServerRoleEntity serverRoleEntity = new ServerRoleEntity();
        serverRoleEntity.setRoleGuid(roleNotStored);
        serverRoleEntity.setServerGuid(serverEntity.get());

        repositoryContainer.getServerRoleRepository().save(serverRoleEntity);

        List<Member> membersWithRoleToCreate = guild.getMembersWithRoles(guild.getRoleById(roleNotStored));
        if (!membersWithRoleToCreate.isEmpty()) {
          for (Member memberToUpdate : membersWithRoleToCreate) {
            persistRoleUpdateToUser(guild, memberToUpdate);
          }
        }
      }
    }
  }
}
