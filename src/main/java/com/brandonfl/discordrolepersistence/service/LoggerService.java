package com.brandonfl.discordrolepersistence.service;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggerService {

  public void logRolesGivedBack(GuildMemberJoinEvent joinEvent, ServerEntity serverEntity, Set<Role> roles) {
    if (!roles.isEmpty() && (serverEntity.getLogChannel() != null || serverEntity.getWelcomeBackChannel() != null)) {
      final StringBuilder rolesGivedBackStringBuilder = new StringBuilder();
      roles.forEach(role -> rolesGivedBackStringBuilder.append("- ").append(role.getAsMention()).append("\n"));

      if (serverEntity.getLogChannel() != null) {
        Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(joinEvent.getGuild(), serverEntity);
        if (logChannel.isPresent()) {
          EmbedBuilder logEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA())
              .setAuthor("Role backup for " + joinEvent.getMember().getEffectiveName(), null, joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .appendDescription("User id : " + joinEvent.getMember().getUser().getId() + "\n\n" + rolesGivedBackStringBuilder.toString());

          logChannel.get().sendMessage(logEmbedBuilder.build()).queue();
        }
      }

      if (serverEntity.getWelcomeBackChannel() != null) {
        Optional<TextChannel> welcomeBackChannel = DiscordBotUtils.getWelcomeBackChannel(joinEvent.getGuild(), serverEntity);
        if (welcomeBackChannel.isPresent()) {
          EmbedBuilder welcomeBackEmbedBuilder = DiscordBotUtils.getGenericEmbed(joinEvent.getJDA())
              .setTitle("Welcome back " + joinEvent.getMember().getEffectiveName())
              .setThumbnail(joinEvent.getMember().getUser().getEffectiveAvatarUrl())
              .addField("Here are your old roles that have been given back to you", rolesGivedBackStringBuilder.toString(), true);

          welcomeBackChannel.get().sendMessage(joinEvent.getMember().getAsMention()).queue();
          welcomeBackChannel.get().sendMessage(welcomeBackEmbedBuilder.build()).queue();
        }
      }
    }
  }
}
