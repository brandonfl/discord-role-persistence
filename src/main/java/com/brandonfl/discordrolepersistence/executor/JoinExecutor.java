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
  @Async("asyncJoinExecutor")
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

      StringBuilder logStringBuilder = new StringBuilder();
      StringBuilder welcomeBackStringBuilder = new StringBuilder();
      for (Role role : rolesToAddToUser) {
        if (role.hasPermission(Permission.ADMINISTRATOR)) {
          logStringBuilder.append(":no_entry: ").append(role.getName()).append(" (admin permissions are not backup)").append("\n");
        } else {
          boolean success = true;
          try {
            joinEvent.getGuild().addRoleToMember(joinEvent.getMember(), role).complete();
          } catch (Exception e) {
            success = false;
          } finally {
            logStringBuilder
                .append(success ? ":white_check_mark: " : ":warning: ")
                .append(role.getName()).append(success ? "" : " (not enough permissions, please put the bot upper into the role hierarchy)")
                .append("\n");
            if (success) {
              welcomeBackStringBuilder.append("- ").append(role.getAsMention()).append("\n");
            }
          }
        }
      }

      ServerEntity serverEntity = serverUserEntity.get().getServerGuid();
      if (logStringBuilder.length() != 0) {
        EmbedBuilder logEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA());
        Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(joinEvent.getGuild(), serverEntity);
        if (logChannel.isPresent()) {
          logEmbedBuilder
              .setAuthor("Role backup for " + joinEvent.getMember().getEffectiveName(), null, joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .appendDescription("User id : " + joinEvent.getMember().getUser().getId() + "\n\n" + logStringBuilder.toString());

          logChannel.get().sendMessage(logEmbedBuilder.build()).queue();
        }
      }

      if (welcomeBackStringBuilder.length() != 0) {
        EmbedBuilder welcomeBackEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA());
        Optional<TextChannel> welcomeBackChannel = DiscordBotUtils.getWelcomeBackChannel(joinEvent.getGuild(), serverEntity);
        if (welcomeBackChannel.isPresent()) {
          welcomeBackEmbedBuilder
              .setTitle("Welcome back " + joinEvent.getMember().getEffectiveName())
              .setThumbnail(joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .addField("Here are your old roles that have been given back to you", welcomeBackStringBuilder.toString(), true);

          welcomeBackChannel.get().sendMessage(joinEvent.getMember().getAsMention()).queue();
          welcomeBackChannel.get().sendMessage(welcomeBackEmbedBuilder.build()).queue();
        }
      }
    }
  }

}
