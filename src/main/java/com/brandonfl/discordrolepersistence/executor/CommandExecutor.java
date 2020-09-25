package com.brandonfl.discordrolepersistence.executor;

import com.brandonfl.discordrolepersistence.config.BotProperties;
import com.brandonfl.discordrolepersistence.db.entity.ServerEntity;
import com.brandonfl.discordrolepersistence.db.entity.ServerRoleEntity;
import com.brandonfl.discordrolepersistence.db.repository.RepositoryContainer;
import com.brandonfl.discordrolepersistence.utils.DiscordBotUtils;
import java.util.Optional;
import javax.transaction.Transactional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CommandExecutor {

  private final RepositoryContainer repositoryContainer;

  @Autowired
  public CommandExecutor(
      RepositoryContainer repositoryContainer) {
    this.repositoryContainer = repositoryContainer;
  }

  @Transactional
  @Async("asyncCommandExecutor")
  public void executeCommand(GuildMessageReceivedEvent event, BotProperties botProperties) {
    changePrefix(event);
    getHelp(event, botProperties);
    getPing(event);
    changeLogChannel(event);
    changeWelcomeBackChannel(event);
    lockRole(event);
    unlockRole(event);
  }

  private void getHelp(GuildMessageReceivedEvent event, BotProperties botProperties) {
    final String command = "help";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository().findByGuid(event.getGuild().getIdLong());
      if (serverEntity.isPresent() && DiscordBotUtils.verifyCommand(serverEntity.get(), msg, command)) {
        EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed();
        embedBuilder
            .setAuthor("Discord Role Persistence commands")
            .addField("Commands",
                "ping\n"
                    + "log `#Channel`\n"
                    + "log disable\n"
                    + "welcome-back `#Channel`\n"
                    + "welcome-back disable\n"
                    + "lock `#Role`\n"
                    + "lock `roleId`\n"
                    + "unlock `#Role`\n"
                    + "unlock `roleId`"
                , true)
            .addField("Description",
                "Get discord bot ping\n"
                    + "Change logger channel\n"
                    + "Disable logger channel\n"
                    + "Disable welcome back channel\n"
                    + "Preventing the role from being rollback\n"
                    + "Preventing the role with id from being rollback \n"
                    + "Allows the role to be rollback\n"
                    + "Allows the role with id to be rollback\n"
                , true)
            .addField("Version", botProperties.getSetting().getVersion(), false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
      }
    }
  }

  private void getPing(GuildMessageReceivedEvent event) {
    final String command = "ping";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> serverEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (serverEntity.isPresent() && DiscordBotUtils.verifyCommand(serverEntity.get(), msg, command)) {
        MessageChannel channel = event.getChannel();
        long time = System.currentTimeMillis();
        channel.sendMessage("Pong!") /* => RestAction<Message> */
            .queue(response /* => Message */ -> {
              response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
            });
      }
    }
  }

  private void changePrefix(GuildMessageReceivedEvent event) {
    final String command = "prefix";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getContentRaw().split(" ").length == 2) {
            final String newPrefix = msg.getContentRaw().split(" ")[1].trim();

            ServerEntity serverEntity = possibleServerEntity.get();
            serverEntity.setCommandPrefix(newPrefix);
            repositoryContainer.getServerRepository().save(serverEntity);

            event.getChannel().sendMessage(":white_check_mark: The command prefix is now `" + newPrefix + "`").queue();

            Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(event.getGuild(), possibleServerEntity.get());
            if (logChannel.isPresent()) {
              EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed();
              embedBuilder
                  .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                  .setTitle(":white_check_mark: Changed command prefix to `" + newPrefix + "`");

              logChannel.get().sendMessage(embedBuilder.build()).queue();
            }
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one prefix").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

  private void changeLogChannel(GuildMessageReceivedEvent event) {
    final String command = "log";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedChannels().size() == 1) {
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setLogChannel(msg.getMentionedChannels().get(0).getIdLong());
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getChannel().sendMessage(":white_check_mark: Log channel has been changed").queue();
          } else if (DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command + " disable")){
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setLogChannel(null);
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getChannel().sendMessage(":white_check_mark: Log channel has been disabled").queue();
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one channel ").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

  private void changeWelcomeBackChannel(GuildMessageReceivedEvent event) {
    final String command = "welcome-back";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedChannels().size() == 1) {
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setWelcomeBackChannel(msg.getMentionedChannels().get(0).getIdLong());
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getChannel().sendMessage(":white_check_mark: Welcome back channel has been changed").queue();
          } else if (DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command + " disable")){
            ServerEntity serverEntityToUpdate = possibleServerEntity.get();
            serverEntityToUpdate.setWelcomeBackChannel(null);
            repositoryContainer.getServerRepository().save(serverEntityToUpdate);

            event.getChannel().sendMessage(":white_check_mark: Welcome back channel has been disabled").queue();
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one channel ").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

  private void lockRole(GuildMessageReceivedEvent event) {
    final String command = "lock";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedRoles().size() == 1 || DiscordBotUtils.verifyCommandFormat( msg, command + "[\\s]+[0-9]+$")) {
            Role role;
            if (msg.getMentionedRoles().size() == 1) {
              role = msg.getMentionedRoles().get(0);
            } else {
              role = event.getGuild().getRoleById(event.getMessage().getContentRaw().split(" ")[1]);
            }

            if (role != null) {
              Optional<ServerRoleEntity> possibleServerRoleEntity = repositoryContainer
                  .getServerRoleRepository()
                  .findByRoleGuidAndRoleGuid(role.getIdLong(), event.getGuild().getIdLong());
              ServerRoleEntity serverRoleEntity;
              if (possibleServerRoleEntity.isPresent()) {
                serverRoleEntity = possibleServerRoleEntity.get();
                if (serverRoleEntity.isBlacklisted()) {
                  event.getChannel().sendMessage(":x: This role is already locked for future rollbacks").queue();
                  return;
                }
              } else {
                serverRoleEntity = new ServerRoleEntity();
                serverRoleEntity.setServerGuid(possibleServerEntity.get());
                serverRoleEntity.setRoleGuid(role.getIdLong());
              }

              serverRoleEntity.setBlacklisted(true);
              repositoryContainer.getServerRoleRepository().save(serverRoleEntity);
              event.getChannel().sendMessage(":white_check_mark: Role " + role.getName() + " is now locked for future rollbacks").queue();

              Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(event.getGuild(), possibleServerEntity.get());
              if (logChannel.isPresent()) {
                EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed();
                embedBuilder
                    .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                    .addField(":white_check_mark: Locked rollbacks for role", role.getName() + " (" + role.getId() + ")", true);

                logChannel.get().sendMessage(embedBuilder.build()).queue();
              }
            } else {
              event.getChannel().sendMessage(":x: This role id is invalid").queue();
            }
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one role").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

  private void unlockRole(GuildMessageReceivedEvent event) {
    final String command = "unlock";
    Message msg = event.getMessage();
    if (DiscordBotUtils.verifyCommandFormat(msg, command)) {
      Optional<ServerEntity> possibleServerEntity = repositoryContainer.getServerRepository()
          .findByGuid(event.getGuild().getIdLong());
      if (possibleServerEntity.isPresent() && DiscordBotUtils.verifyCommand(possibleServerEntity.get(), msg, command)) {
        if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
          if (msg.getMentionedRoles().size() == 1 || DiscordBotUtils.verifyCommandFormat( msg, command + "[\\s]+[0-9]+$")) {
            Role role;
            if (msg.getMentionedRoles().size() == 1) {
              role = msg.getMentionedRoles().get(0);
            } else {
              role = event.getGuild().getRoleById(event.getMessage().getContentRaw().split(" ")[1]);
            }

            if (role != null) {
              Optional<ServerRoleEntity> possibleServerRoleEntity = repositoryContainer
                  .getServerRoleRepository()
                  .findByRoleGuidAndRoleGuid(role.getIdLong(), event.getGuild().getIdLong());
              ServerRoleEntity serverRoleEntity;
              if (possibleServerRoleEntity.isPresent()) {
                serverRoleEntity = possibleServerRoleEntity.get();
                if (!serverRoleEntity.isBlacklisted()) {
                  event.getChannel().sendMessage(":x: This role is already unlocked for future rollbacks").queue();
                  return;
                }
              } else {
                serverRoleEntity = new ServerRoleEntity();
                serverRoleEntity.setServerGuid(possibleServerEntity.get());
                serverRoleEntity.setRoleGuid(role.getIdLong());
              }

              serverRoleEntity.setBlacklisted(false);
              repositoryContainer.getServerRoleRepository().save(serverRoleEntity);
              event.getChannel().sendMessage(":white_check_mark: Role " + role.getName() + " is now unlocked for future rollbacks").queue();

              Optional<TextChannel> logChannel = DiscordBotUtils.getLogChannel(event.getGuild(), possibleServerEntity.get());
              if (logChannel.isPresent()) {
                EmbedBuilder embedBuilder = DiscordBotUtils.getGenericEmbed();
                embedBuilder
                    .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                    .addField(":white_check_mark: Unlocked rollbacks for role", role.getName() + " (" + role.getId() + ")", true);

                logChannel.get().sendMessage(embedBuilder.build()).queue();
              }
            } else {
              event.getChannel().sendMessage(":x: This role id is invalid").queue();
            }
          } else {
            event.getChannel().sendMessage(":x: Please provide one and exactly only one role").queue();
          }
        } else {
          event.getChannel().sendMessage(":octagonal_sign: Only administrators can perform this action").queue();
        }
      }
    }
  }

}
