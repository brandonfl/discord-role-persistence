/*
 * MIT License
 *
 * Copyright (c) 2021 Fontany--Legall Brandon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.brandonfl.discordrolepersistence.discordbot.command.slash;

import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.ERROR_EMOJI;
import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.SUCCESS_EMOJI;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.jagrosh.jdautilities.command.SlashCommand;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.transaction.annotation.Transactional;
import xyz.brandonfl.throwableoptional.ThrowableOptional;

public class ChangeWelcomeBackChannelCommand extends SlashCommand {

  private static final String CHANNEL_ARGUMENT_NAME = "channel";
  private final RepositoryContainer repositoryContainer;

  public ChangeWelcomeBackChannelCommand(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;

    this.name = "welcome-back";
    this.help = "Change or disable welcome-back channel.";
    this.options = List
        .of(new OptionData(OptionType.CHANNEL, CHANNEL_ARGUMENT_NAME, "The channel to use to send welcome-back messages"));
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
  }

  @Override
  @Transactional
  public void execute(SlashCommandEvent event) {
    event.deferReply().queue();

    if (event.getGuild() == null) {
      event
          .getHook()
          .editOriginalFormat("%s Please run this command into a server", ERROR_EMOJI)
          .queue();
      return;
    }

    GuildChannel channelArgument = ThrowableOptional
        .of(() -> Objects.requireNonNull(event.getOption(CHANNEL_ARGUMENT_NAME)).getAsChannel())
        .orElse(null);

    if (channelArgument != null) {
      if (!ChannelType.TEXT.equals(channelArgument.getType())) {
        event
            .getHook()
            .editOriginalFormat("%s Welcome-back channel need to be a text channel", ERROR_EMOJI)
            .queue();
        return;
      }

      if (!event.getGuild().getSelfMember().hasPermission(channelArgument, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
        event
            .getHook()
            .editOriginalFormat("%s It seems that the bot dont have talk access to this channel", ERROR_EMOJI)
            .queue();
        return;
      }
    }

    ServerEntity serverEntity = repositoryContainer.getServerRepository()
        .findByGuid(event.getGuild().getIdLong())
        .orElse(new ServerEntity());

    serverEntity.setGuid(event.getGuild().getIdLong());
    serverEntity.setWelcomeBackChannel(channelArgument == null ? null : channelArgument.getIdLong());
    repositoryContainer.getServerRepository().save(serverEntity);

    if (channelArgument != null) {
      event
          .getHook()
          .editOriginalFormat("%s Welcome-back channel has been changed", SUCCESS_EMOJI)
          .queue();
    } else {
      event
          .getHook()
          .editOriginalFormat("%s Welcome-back channel has been disabled", SUCCESS_EMOJI)
          .queue();
    }
  }
}
