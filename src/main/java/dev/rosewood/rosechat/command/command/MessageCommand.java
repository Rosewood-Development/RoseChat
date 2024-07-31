package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.OfflinePlayerArgumentHandler;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.Bukkit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageCommand extends RoseChatCommand {

    public MessageCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("message")
                .aliases(
                        "msg",
                        "m",
                        "pm",
                        "whisper",
                        "w",
                        "tell",
                        "t"
                )
                .permission("rosechat.message")
                .descriptionKey("command-message-description")
                .arguments(ArgumentsDefinition.builder()
                        .required("player", new OfflinePlayerArgumentHandler(Settings.ALLOW_BUNGEECORD_MESSAGES.get()))
                        .required("message", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String targetName, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            RosePlayer player = new RosePlayer(context.getSender());
            RosePlayer target = this.findPlayer(targetName);
            RosePlayer messagePlayer = new RosePlayer(
                    target == null ? targetName : target.getRealName(),
                    target == null ? "default" : target.getPermissionGroup()
            );

            if (MessageUtils.isMessageEmpty(message)) {
                player.sendLocaleMessage("message-blank");
                return;
            }

            AtomicBoolean canBeMessaged = new AtomicBoolean(true);
            if (target != null && !player.hasPermission("rosechat.togglemessage.bypass")) {
                this.getAPI().getPlayerData(target.getUUID(), data -> {
                    if (data != null && !data.canBeMessaged()) {
                        canBeMessaged.set(false);
                    }
                });
            }

            if (!canBeMessaged.get()) {
                player.sendLocaleMessage("command-togglemessage-cannot-message");
                return;
            }

            MessageUtils.sendPrivateMessage(player, messagePlayer.getRealName(), message);

            if (player.isPlayer()) {
                player.getPlayerData().setReplyTo(messagePlayer.getRealName());
                player.getPlayerData().save();
            }

            if (this.getAPI().isBungee())
                this.getAPI().getBungeeManager().sendUpdateReply(player.getRealName(), messagePlayer.getRealName());

            if (!messagePlayer.isPlayer() && !messagePlayer.isConsole())
                return;

            if (target != null) {
                PlayerData targetData = target.getPlayerData();
                if (targetData == null)
                    return;

                targetData.setReplyTo(player.getRealName());
                targetData.save();
            }
        });
    }

}
