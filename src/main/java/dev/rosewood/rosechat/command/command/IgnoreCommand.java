package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class IgnoreCommand extends RoseChatCommand {

    public IgnoreCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("ignore")
                .descriptionKey("command-ignore-description")
                .permission("rosechat.ignore")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("player", RoseChatArgumentHandlers.OFFLINE_PLAYER)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            RosePlayer player = new RosePlayer(context.getSender());
            RosePlayer target = this.findPlayer(targetName);

            if (target == null) {
                player.sendLocaleMessage("invalid-argument",
                        StringPlaceholders.of("message",
                                this.getLocaleManager().getLocaleMessage("argument-handler-player")));
                return;
            }

            if (target.hasPermission("rosechat.ignore.bypass")) {
                player.sendLocaleMessage("command-ignore-cannot-ignore");
                return;
            }

            PlayerData playerData = player.getPlayerData();

            this.getAPI().getPlayerData(target.getUUID(), (targetData) -> {
                String name = target.getName();

                if (playerData.getIgnoringPlayers().contains(target.getUUID())) {
                    playerData.unignore(target.getUUID());
                    player.sendLocaleMessage("command-ignore-unignored",
                            StringPlaceholders.of("player", name));
                } else {
                    playerData.ignore(target.getUUID());
                    player.sendLocaleMessage("command-ignore-ignored",
                            StringPlaceholders.of("player", name));
                }
            });
        });
    }

}
