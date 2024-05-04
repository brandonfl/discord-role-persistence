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
import static com.brandonfl.discordrolepersistence.discordbot.DiscordBot.WARNING_EMOJI;

import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.entity.role.ServerRoleAdminEnableBackupEntity;
import com.brandonfl.discordrolepersistence.db.entity.role.ServerRoleBlacklistEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import com.jagrosh.jdautilities.command.SlashCommand;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.transaction.annotation.Transactional;
import xyz.brandonfl.throwableoptional.ThrowableOptional;

public class EnableRollbackRoleCommand extends SlashCommand {

  private static final String ROLE_ARGUMENT_NAME = "role";
  private static final String FORCE_ARGUMENT_NAME = "force";
  private final RepositoryContainer repositoryContainer;
  private final DiscordBotUtils discordBotUtils;

  public EnableRollbackRoleCommand(
      RepositoryContainer repositoryContainer,
      DiscordBotUtils discordBotUtils
  ) {
    this.repositoryContainer = repositoryContainer;
    this.discordBotUtils = discordBotUtils;

    this.name = "rollback-enable";
    this.help = "Allows role to be reapplied at future member join. By default, roles reapplied except admin roles.";
    this.options = List
        .of(new OptionData(OptionType.ROLE, ROLE_ARGUMENT_NAME, "The role to apply at future member join - required").setRequired(true),
            new OptionData(OptionType.BOOLEAN, FORCE_ARGUMENT_NAME, "Force admin role to be reapplied at future member join.").setRequired(false));
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

    Role roleArgument = ThrowableOptional
        .of(() -> Objects.requireNonNull(event.getOption(ROLE_ARGUMENT_NAME)).getAsRole())
        .orElse(null);

    if (roleArgument == null) {
      event
          .getHook()
          .editOriginalFormat("%s Please provide one and exactly only one role", ERROR_EMOJI)
          .queue();
      return;
    }

    final boolean forceArgument = ThrowableOptional
        .of(() -> Objects.requireNonNull(event.getOption(FORCE_ARGUMENT_NAME)).getAsBoolean())
        .orElse(false);

    if (roleArgument.hasPermission(Permission.ADMINISTRATOR) && !forceArgument) {
      event
          .getHook()
          .editOriginalFormat("%s This role is currently an administrator role. You can force the reapply with force option.", WARNING_EMOJI)
          .queue();
      return;
    }

    repositoryContainer
        .getServerRoleBlacklistRepository()
        .deleteByServerGuidAndRoleGuid(event.getGuild().getIdLong(), roleArgument.getIdLong());

    if (forceArgument) {
      ServerRoleAdminEnableBackupEntity serverRoleAdminEnableBackupEntity = repositoryContainer
          .getServerRoleAdminEnableBackupRepository()
          .findByServerGuidAndRoleGuid(event.getGuild().getIdLong(), roleArgument.getIdLong())
          .orElse(new ServerRoleAdminEnableBackupEntity());

      serverRoleAdminEnableBackupEntity.setServerGuid(event.getGuild().getIdLong());
      serverRoleAdminEnableBackupEntity.setRoleGuid(roleArgument.getIdLong());
      repositoryContainer.getServerRoleAdminEnableBackupRepository().save(serverRoleAdminEnableBackupEntity);
    }

    event
        .getHook()
        .editOriginalFormat("%s Role %s is now reapplied at future member join", SUCCESS_EMOJI, roleArgument.getName())
        .queue();

    Optional<TextChannel> logChannel = discordBotUtils.getLogChannel(event.getGuild());
    if (logChannel.isPresent()) {
      EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed(event.getJDA());
      embedBuilder
          .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
          .addField(":unlock: Role reapplied at future member join",
              roleArgument.getName() + " (" + roleArgument.getId() + ")", true);

      logChannel.get().sendMessage(embedBuilder.build()).queue();
    }
  }
}
