package com.brandonfl.demobot.discordbot.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.brandonfl.demobot.discordbot.DiscordBot;

public class PingPong extends ListenerAdapter {
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    Message msg = event.getMessage();
    if (msg.getContentRaw().equals(DiscordBot.PREFIX + "ping")) {
      MessageChannel channel = event.getChannel();
      long time = System.currentTimeMillis();
      channel.sendMessage("Pong!") /* => RestAction<Message> */
          .queue(response /* => Message */ -> {
            response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
          });
    }
  }
}