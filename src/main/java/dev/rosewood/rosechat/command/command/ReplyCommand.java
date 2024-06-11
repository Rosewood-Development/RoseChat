package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplyCommand extends RoseChatCommand {

    public ReplyCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("r")
                .aliases("reply")
                .descriptionKey("command-reply-description")
                .permission("rosechat.reply")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("message", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String message) {
        RosePlayer player = new RosePlayer(context.getSender());
        String targetName = player.getPlayerData().getReplyTo();
        if (targetName == null) {
            player.sendLocaleMessage("command-reply-no-one");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        AtomicBoolean canBeMessaged = new AtomicBoolean(true);
        if (!context.getSender().hasPermission("rosechat.togglemessage.bypass")) {
            this.getAPI().getPlayerData(target.getUniqueId(), data -> {
                if (data != null && !data.canBeMessaged()) {
                    player.sendLocaleMessage("command-togglemessage-cannot-message");
                    canBeMessaged.set(false);
                }
            });
        }

        if (!canBeMessaged.get())
            return;

        MessageUtils.sendPrivateMessage(player, targetName, message);
    }

}
