package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class MemberEvent extends ListenerAdapter {

  private final PersistExecutor persistExecutor;

  @Override
  public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
    persistExecutor.persistRoleUpdateToUser(event.getGuild(), event.getMember(), event, null);
  }

  @Override
  public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
    persistExecutor.persistRoleUpdateToUser(event.getGuild(), event.getMember(), null, event);
  }
}
