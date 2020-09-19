package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;
  private final PersistExecutor persistExecutor;

  public ServerEvent(
      RepositoryContainer repositoryContainer,
      PersistExecutor persistExecutor) {
    this.repositoryContainer = repositoryContainer;
    this.persistExecutor = persistExecutor;
  }

  @Override
  public void onGuildJoin(@Nonnull GuildJoinEvent event) {
    if (!repositoryContainer.getServerRepository()
        .findById(event.getGuild().getIdLong()).isPresent()) {
      persistExecutor.persistNewServer(event.getGuild());
    }
  }

  @Override
  public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
    repositoryContainer.getServerRepository()
        .findById(event.getGuild().getIdLong())
        .ifPresent(entity -> repositoryContainer.getServerRepository().delete(entity));
  }
}
