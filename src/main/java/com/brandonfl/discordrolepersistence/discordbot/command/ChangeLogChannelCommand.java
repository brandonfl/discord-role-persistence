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

package com.brandonfl.discordrolepersistence.discordbot.command;

import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.ERROR_EMOJI;
import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.SUCCESS_EMOJI;
import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.WARNING_EMOJI;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite.Channel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.transaction.annotation.Transactional;
import xyz.brandonfl.throwableoptional.ThrowableOptional;

public class ChangeLogChannelCommand extends SlashCommand {

  private static final String CHANNEL_ARGUMENT_NAME = "channel";
  private final RepositoryContainer repositoryContainer;

  public ChangeLogChannelCommand(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;

    this.name = "log";
    this.help = "Change or disable logger channel.";
    this.options = List
        .of(new OptionData(OptionType.CHANNEL, CHANNEL_ARGUMENT_NAME, "The channel to use to send logs"));
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
  }

  @Override
  @Transactional
  public void execute(SlashCommandEvent event) {
    event.deferReply().queue();
    GuildChannel channelArgument = ThrowableOptional
        .of(() -> Objects.requireNonNull(event.getOption(CHANNEL_ARGUMENT_NAME)).getAsGuildChannel())
        .orElse(null);

    if (event.getGuild() == null) {
      event
          .getHook()
          .editOriginalFormat("%s Current server not existing", WARNING_EMOJI)
          .queue();
    } else {
      ServerEntity serverEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong()).orElse(null);
      if (serverEntity != null) {
        if (channelArgument != null) {

          if (ChannelType.TEXT.equals(channelArgument.getType())) {
            event
                .getHook()
                .editOriginalFormat("%s Log channel need to be a text channel", SUCCESS_EMOJI)
                .queue();
          } else if (!event.getGuild().getSelfMember().hasPermission(channelArgument, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE)) {
            event
                .getHook()
                .editOriginalFormat("%s It seems that the bot dont have talk access to this channel", ERROR_EMOJI)
                .queue();
          } else {
            serverEntity.setLogChannel(channelArgument.getIdLong());
            repositoryContainer.getServerRepository().save(serverEntity);

            event
                .getHook()
                .editOriginalFormat("%s Log channel has been changed", SUCCESS_EMOJI)
                .queue();
          }
        } else {
          serverEntity.setLogChannel(null);
          repositoryContainer.getServerRepository().save(serverEntity);

          event
              .getHook()
              .editOriginalFormat("%s Log channel has been disabled", SUCCESS_EMOJI)
              .queue();
        }
      } else {
        event
            .getHook()
            .editOriginalFormat("%s \"Current server not found", WARNING_EMOJI)
            .queue();
      }
    }
  }
}
