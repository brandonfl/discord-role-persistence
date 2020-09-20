package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.executor.JoinExecutor;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserJoinEvent extends ListenerAdapter {

  private final JoinExecutor joinExecutor;

  public UserJoinEvent(JoinExecutor joinExecutor) {
    this.joinExecutor = joinExecutor;
  }

  @Override
  public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
    joinExecutor.backupRoleOfMember(event);
  }
}
