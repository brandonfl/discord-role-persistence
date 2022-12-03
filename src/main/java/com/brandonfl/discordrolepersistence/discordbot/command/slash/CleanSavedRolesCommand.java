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
import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.WARNING_EMOJI;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.jagrosh.jdautilities.command.SlashCommand;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.transaction.annotation.Transactional;
import xyz.brandonfl.throwableoptional.ThrowableOptional;

public class CleanSavedRolesCommand extends SlashCommand {

  private static final String USER = "user";
  private final RepositoryContainer repositoryContainer;

  public CleanSavedRolesCommand(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;

    this.name = "clean-saved-roles";
    this.help = "Clean saved roles";
    this.options = List
        .of(new OptionData(OptionType.USER, USER,"Clean saved roles for a specific user").setRequired(false));
    this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
    this.guildOnly = true;
  }

  @Override
  @Transactional
  public void execute(SlashCommandEvent event) {
    event.deferReply(true).queue();
    User userArgument = ThrowableOptional
        .of(() -> Objects.requireNonNull(event.getOption(USER)).getAsUser())
        .orElse(null);

    ServerEntity serverEntity = event.getGuild() != null
        ? repositoryContainer.getServerRepository().findByGuid(event.getGuild().getIdLong()).orElse(null)
        : null;

    if (serverEntity == null) {
      event
          .getHook()
          .setEphemeral(true)
          .editOriginalFormat("%s Current server not existing", ERROR_EMOJI)
          .queue();
      return;
    }


    if (userArgument != null) {
      int deletedCounter = repositoryContainer.getServerUserRepository()
          .deleteAllByServerGuidAndUserGuid(serverEntity, userArgument.getIdLong());

      if (deletedCounter > 0) {
        event
            .getHook()
            .setEphemeral(true)
            .editOriginalFormat("%s Saved roles for %s as been cleaned", WARNING_EMOJI,
                userArgument.getAsMention())
            .queue();
      } else {
        event
            .getHook()
            .setEphemeral(true)
            .editOriginalFormat("%s User %s dont have saved roles into this server", WARNING_EMOJI,
                userArgument.getAsMention())
            .queue();
      }
    } else {
      repositoryContainer.getServerUserRepository().deleteAllByServerGuid(serverEntity);
      event
          .getHook()
          .setEphemeral(true)
          .editOriginalFormat("%s All saved roles as been cleaned", WARNING_EMOJI)
          .queue();
    }
  }
}
