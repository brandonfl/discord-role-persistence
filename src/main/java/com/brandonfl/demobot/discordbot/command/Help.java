package com.brandonfl.demobot.discordbot.command;

import com.brandonfl.demobot.config.BotProperties;
import com.brandonfl.demobot.discordbot.DiscordBot;
import java.awt.Color;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
public class Help extends ListenerAdapter {
  private final BotProperties botProperties;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {

    Message msg = event.getMessage();
    if (msg.getContentRaw().equals(DiscordBot.PREFIX + "help")) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder
          .setColor(new Color(52, 152, 219))
          .setAuthor("Demo")

          .addField("Commands",
              "ping\n"
              , true)

          .addField("Description",
              "Get discord bot ping\n"
              , true)

          .addField("Version", botProperties.getSetting().getVersion(), false);

      event.getChannel().sendMessage(embedBuilder.build()).queue();

    }
  }
}