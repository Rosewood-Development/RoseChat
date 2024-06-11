package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.ChatColorArgumentHandler;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class NickColorCommand extends RoseChatCommand {

    public NickColorCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("nickcolor")
                .aliases("nickcolour")
                .descriptionKey("command-nickcolor-description")
                .permission("rosechat.nickcolor")
                .arguments(ArgumentsDefinition.builder()
                        .optional("player", RoseChatArgumentHandlers.ROSE_PLAYER,
                                ArgumentCondition.hasPermission("rosechat.nickcolor.others"))
                        .required("color", new ChatColorArgumentHandler(PermissionArea.NICKNAME))
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String color) {
        RosePlayer player = new RosePlayer(context.getSender());
        if (player.isConsole()) {
            player.sendLocaleMessage("only-player");
            return;
        }

        if (color.isEmpty()) {
            this.removeColor(player, player);
            return;
        }

        if (!MessageUtils.canColor(player, color, PermissionArea.NICKNAME)) {
            player.sendLocaleMessage("no-permission");
            return;
        }

        this.setColor(player, player, color);
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target, String color) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (color.isEmpty()) {
            this.removeColor(player, target);
            return;
        }

        this.setColor(player, target, color);
    }

    private void removeColor(RosePlayer player, RosePlayer target) {
        boolean success = target.removeNicknameColor();
        if (!success)
            return;

        if (!player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-nickcolor-removed-player",
                    StringPlaceholders.of("player", player.getName()));
        target.sendLocaleMessage("command-nickcolor-removed");
    }

    private void setColor(RosePlayer player, RosePlayer target, String color) {
        boolean success = target.setNicknameColor(color);
        if (!success)
            return;

        if (player.isConsole() || !player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-nickcolor-player",
                    StringPlaceholders.of(
                            "player", target.getRealName(),
                            "name", target.getName()
                    ));
        target.sendLocaleMessage("command-nickcolor-success",
                StringPlaceholders.of("name", target.getName()));
    }

}
