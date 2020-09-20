package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerUserEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.transaction.Transactional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
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
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());

      StringBuilder stringBuilder = new StringBuilder();
      for (Role role : rolesToAddToUser) {
        if (role.hasPermission(Permission.ADMINISTRATOR)) {
          stringBuilder.append(":no_entry: ").append(role.getName()).append(" (admin permissions are not backup)").append("\n");
        } else {
          boolean success = true;
          try {
            joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).complete();
          } catch (Exception e) {
            success = false;
          } finally {
            stringBuilder
                .append(success ? ":white_check_mark: " : ":warning: ")
                .append(role.getName()).append(success ? "" : " (issue or not enough permissions)")
                .append("\n");
          }
        }
      }

      EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed();
      ServerEntity serverEntity = serverUserEntity.get().getServerGuid();
      Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(joinEvent.getGuild(), serverEntity);
      if (logChannel.isPresent()) {
        embedBuilder
            .setAuthor(joinEvent.getMember().getEffectiveName(), null, joinEvent.getMember().getUser().getEffectiveAvatarUrl())
            .addField("Roles", stringBuilder.toString(), true);

        logChannel.get().sendMessage(embedBuilder.build()).queue();
      }

    }
  }

}
