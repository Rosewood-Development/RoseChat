package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;

public class UnmuteCommand extends RoseChatCommand {

    public UnmuteCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("unmute")
                .descriptionKey("command-unmute-description")
                .permission("rosechat.unmute")
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
            RosePlayer target = this.findPlayer(targetName);
            if (target == null) {
                this.getLocaleManager().sendComponentMessage(context.getSender(), "invalid-argument",
                        StringPlaceholders.of("message",
                                this.getLocaleManager().getLocaleMessage("argument-handler-player")));
                return;
            }

            target.getPlayerData((data) -> {
                if (data == null)
                    return;

                data.unmute();
                data.save();

                String name = target.getName();
                this.getLocaleManager().sendComponentMessage(context.getSender(), "command-unmute-success",
                        StringPlaceholders.of("player", name));

                if (target.isPlayer())
                    this.getLocaleManager().sendComponentMessage(target, "command-mute-unmuted");
            });
        });
    }

}
