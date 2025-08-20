package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.command.RoseChatCommand;
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
import java.util.regex.Matcher;
import org.bukkit.ChatColor;

public class ChatColorCommand extends RoseChatCommand {

    public ChatColorCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("chatcolor")
                .aliases(
                        "color",
                        "chatcolour",
                        "colour"
                )
                .descriptionKey("command-chatcolor-description")
                .permission("rosechat.chatcolor")
                .arguments(ArgumentsDefinition.builder()
                        .optional("player", RoseChatArgumentHandlers.ROSE_PLAYER,
                                ArgumentCondition.hasPermission("rosechat.chatcolor.others"))
                        .required("color", RoseChatArgumentHandlers.CHAT_COLOR)
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

        if (color.isEmpty()) {
            this.removeColor(player, player);
            return;
        }

        if (!MessageUtils.canColor(player, color, PermissionArea.CHATCOLOR)) {
            player.sendLocaleMessage("no-permission");
            return;
        }

        color = MessageUtils.stripShaderColors(color);
        this.setColor(player, player, color);
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target, String color) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (color.isEmpty()) {
            this.removeColor(player, target);
            return;
        }

        color = MessageUtils.stripShaderColors(color);
        this.setColor(player, target, color);
    }

    private void removeColor(RosePlayer player, RosePlayer target) {
        PlayerData targetData = target.getPlayerData();

        targetData.setColor("");
        targetData.save();

        if (player.isConsole() || !player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-chatcolor-removed-player",
                    StringPlaceholders.of("player", target.getName()));
        target.sendLocaleMessage("command-chatcolor-removed");
    }

    private void setColor(RosePlayer player, RosePlayer target, String color) {
        PlayerData targetData = target.getPlayerData();
        String colorStr = color.startsWith("<") && color.endsWith(">") ?
                color.substring(1, color.length() - 1).replace('_', ' ') : MessageUtils.ESCAPE_CHAR + color;

        // Allow color filters.
        Filter filter = this.getAPI().getFilterByInput(color);
        if (filter != null) {
            if (!filter.colorRetention() || !filter.hasPermission(player)
                    || !player.hasPermission("rosechat.filters.chatcolor")) {
                player.sendLocaleMessage("no-permission");
                return;
            }

            color = filter.replacement();
        }

        targetData.setColor(color);
        targetData.save();

        if (color.startsWith("<r")) {
            Matcher matcher = MessageUtils.RAINBOW_PATTERN.matcher(color);
            if (matcher.find())
                colorStr = this.getLocaleManager().getMessage("command-chatcolor-rainbow");
        } else if (color.startsWith("<g")) {
            Matcher matcher = MessageUtils.GRADIENT_PATTERN.matcher(color);
            if (matcher.find())
                colorStr = this.getLocaleManager().getMessage("command-chatcolor-gradient");
        }

        if (player.getUUID() == null || !player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-chatcolor-player",
                    StringPlaceholders.of(
                            "player", target.getName(),
                            "color", ChatColor.stripColor(color + colorStr)
                    ));
        target.sendLocaleMessage("command-chatcolor-success",
                StringPlaceholders.of("color", ChatColor.stripColor(color + colorStr)));
    }

}
