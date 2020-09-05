package com.brandonfl.demobot.discordbot.event;

import com.brandonfl.demobot.db.entity.DiscordBotConfigEntity;
import com.brandonfl.demobot.discordbot.model.BotConfig;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.brandonfl.demobot.db.repository.RepositoryContainer;

public class UserCounter extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;

  public UserCounter(RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;

  }

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    for (Guild guild : event.getJDA().getGuilds()) {
      updateMemberCounter(guild);
    }
  }

  @Override
  public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
    updateMemberCounter(event.getGuild());
  }

  @Override
  public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
    updateMemberCounter(event.getGuild());
  }

  private void updateMemberCounter(Guild guild) {
    Optional<DiscordBotConfigEntity> counterChannel = repositoryContainer.getDiscordBotConfigRepository()
        .findByTokenAndGuid(BotConfig.MEMBER_COUNTER_CHANNEL.getToken(), guild.getId());
    if (counterChannel.isPresent()) {
      GuildChannel channel = guild.getGuildChannelById(counterChannel.get().getValue());
      if (channel != null) {
        channel.getManager().setName("Members: " + guild.getMemberCount()).queue();
      }
    } else {
      guild.createVoiceChannel("Members: " + guild.getMemberCount())
          .queue(channelCreated -> {
            DiscordBotConfigEntity discordBotConfigEntity = new DiscordBotConfigEntity();
            discordBotConfigEntity.setToken(BotConfig.MEMBER_COUNTER_CHANNEL.getToken());
            discordBotConfigEntity.setGuid(guild.getId());
            discordBotConfigEntity.setValue(channelCreated.getId());
            repositoryContainer.getDiscordBotConfigRepository().save(discordBotConfigEntity);
          });
    }
  }

}
