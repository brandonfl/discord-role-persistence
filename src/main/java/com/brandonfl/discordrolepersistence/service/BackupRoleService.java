package com.brandonfl.discordrolepersistence.service;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackupRoleService  {

  private final RepositoryContainer repositoryContainer;
  private final LoggerService loggerService;
  private final ExecutorService executorService;

  @Autowired
  public BackupRoleService(BotProperties botProperties,
      RepositoryContainer repositoryContainer,
      LoggerService loggerService) {
    this.repositoryContainer = repositoryContainer;
    this.loggerService = loggerService;

    this.executorService = Executors.newFixedThreadPool(botProperties.getSetting().getBackup().getThreadNumber());
  }

  public void execute(@Nonnull GuildMemberJoinEvent joinEvent) {
    if (executorService != null) {
      executorService.execute(() -> backupRoles(joinEvent));
    }
  }

  private void backupRoles(@Nonnull GuildMemberJoinEvent joinEvent) {
    ServerUserEntity serverUserEntity = repositoryContainer
        .getServerUserRepository()
        .findByUserGuidAndServerGuid(joinEvent.getMember().getIdLong(), joinEvent.getGuild().getIdLong())
        .orElse(null);

    if (serverUserEntity != null && serverUserEntity.getRoleEntities() != null && !serverUserEntity.getRoleEntities().isEmpty()) {
      final int botUpperRole = DiscordBotUtils.getUpperRole(joinEvent.getGuild().getSelfMember().getRoles());
      final Set<Role> rolesToAddToUser = serverUserEntity
          .getRoleEntities()
          .stream()
          .filter(serverRoleEntity -> !serverRoleEntity.isBlacklisted())
          .map(serverRoleEntity -> joinEvent.getGuild().getRoleById(serverRoleEntity.getRoleGuid()))
          .filter(role -> role != null && !role.hasPermission(Permission.ADMINISTRATOR) && (botUpperRole > role.getPosition()))
          .collect(Collectors.toSet());

      rolesToAddToUser.forEach(role -> {
        joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).queue();
      });

      loggerService.logRolesGivedBack(joinEvent, serverUserEntity.getServerGuid(), rolesToAddToUser);
    }
  }
}
