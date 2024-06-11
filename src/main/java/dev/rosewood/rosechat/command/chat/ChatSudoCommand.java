package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.ChannelArgumentHandler;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ChatSudoCommand extends RoseChatCommand {
    public ChatSudoCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("sudo")
                .descriptionKey("command-chat-sudo-description")
                .permission("rosechat.chat.sudo")
                .arguments(ArgumentsDefinition.builder()
                        .required("player", RoseChatArgumentHandlers.OFFLINE_PLAYER)
                        .required("channel", new ChannelArgumentHandler(false))
                        .required("message", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, String targetName, Channel channel, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            RosePlayer target = this.findPlayer(targetName);
            RosePlayer sudoPlayer = new RosePlayer(
                    target == null ? targetName : target.getName(),
                    target == null ? "default": target.getPermissionGroup()
            );

            sudoPlayer.quickChat(channel, message);
        });
    }

}
