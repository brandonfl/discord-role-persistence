package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerRoleEvent extends ListenerAdapter {

  private final PersistExecutor persistExecutor;

  public ServerRoleEvent(
      PersistExecutor persistExecutor) {
    this.persistExecutor = persistExecutor;
  }

  @Override
  public void onRoleCreate(@Nonnull RoleCreateEvent event) {
    persistExecutor.createNewRoles(event.getGuild(), event);
  }

  @Override
  public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
    persistExecutor.deleteOldRoles(event.getGuild(), event);
  }
}
