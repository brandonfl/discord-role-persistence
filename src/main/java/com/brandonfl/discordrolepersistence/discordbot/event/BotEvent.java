package com.brandonfl.discordrolepersistence.discordbot.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class BotEvent extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(BotEvent.class);
  private final PersistExecutor persistExecutor;

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    logger.info("Bot ready !");
    for (Guild guild : event.getJDA().getGuilds()) {
      persistExecutor.persistGuildUpdate(guild);
    }
  }

  @Override
  public void onReconnect(@Nonnull ReconnectedEvent event) {
    logger.warn("Bot reconnected !");
    for (Guild guild : event.getJDA().getGuilds()) {
      persistExecutor.persistGuildUpdate(guild);
    }
  }
}
