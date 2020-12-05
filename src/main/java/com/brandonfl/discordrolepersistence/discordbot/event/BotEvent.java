package com.brandonfl.discordrolepersistence.discordbot.event;

import com.brandonfl.discordrolepersistence.executor.PersistExecutor;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class BotEvent extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(BotEvent.class);
  private final PersistExecutor persistExecutor;

  @Override
  public void onReady(@Nonnull ReadyEvent event) {
    logger.info("Bot ready !");
    persistExecutor.persistGuilds(event.getJDA());
  }

  @Override
  public void onReconnect(@Nonnull ReconnectedEvent event) {
    logger.warn("Bot reconnected !");
    persistExecutor.persistGuilds(event.getJDA());
  }
}
