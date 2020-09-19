package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerEvent extends ListenerAdapter {

  private final RepositoryContainer repositoryContainer;

  public ServerEvent(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Override
  public void onGuildJoin(@Nonnull GuildJoinEvent event) {
    System.out.println("onGuildJoin");
    ServerEntity serverEntity = new ServerEntity();
    serverEntity.setGuid(event.getGuild().getIdLong());
    System.out.println(serverEntity);

    repositoryContainer.getServerRepository().save(serverEntity);
  }

  @Override
  public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
    System.out.println("onGuildLeave");
    repositoryContainer.getServerRepository()
        .findById(event.getGuild().getIdLong())
        .ifPresent(entity -> repositoryContainer.getServerRepository().delete(entity));
  }
}
