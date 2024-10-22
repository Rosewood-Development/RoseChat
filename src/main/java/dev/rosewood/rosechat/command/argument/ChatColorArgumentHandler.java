package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import java.util.ArrayList;
import java.util.List;

public class ChatColorArgumentHandler extends ArgumentHandler<String> {

    private final String permissionArea;

    public ChatColorArgumentHandler(PermissionArea location) {
        super(String.class);

        this.permissionArea = location == PermissionArea.NONE ?
                null : location.toString().toLowerCase();
    }

    @Override
    public String handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        if (input.equalsIgnoreCase("remove") || input.equalsIgnoreCase("off"))
            return "";

        for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.getOutput().hasColorRetention() && replacement.getId().equalsIgnoreCase(input))
                return input;
        }

        // Return if the colorized string contains no color.
        String colorized = HexUtils.colorify(input);
        if (colorized.equalsIgnoreCase(input) || !ChatColor.stripColor(colorized).isEmpty() || input.contains("&r"))
            throw new HandledArgumentException("argument-handler-chatcolor");

        return input;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        List<String> suggestions = new ArrayList<>();

        // Manually add each formatting type if the player has permission.
        suggestions.add("remove");
        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.color." + this.permissionArea) && !Settings.USE_PER_COLOR_PERMISSIONS.get())
            suggestions.add("&a");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.bold." + this.permissionArea))
            suggestions.add("&l");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.strikethrough." + this.permissionArea))
            suggestions.add("&m");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.underline." + this.permissionArea))
            suggestions.add("&n");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.italic." + this.permissionArea))
            suggestions.add("&o");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.magic." + this.permissionArea))
            suggestions.add("&k");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.hex." + this.permissionArea))
            suggestions.add("#FFFFFF");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.rainbow." + this.permissionArea))
            suggestions.add("<r:0.5>");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.gradient." + this.permissionArea))
            suggestions.add("<g:#FFFFFF:#000000>");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.replacements." + this.permissionArea.toLowerCase())) {
            for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
                String permission = replacement.getInput().getPermission() == null ? "rosechat.replacement." + replacement.getId() :
                        replacement.getInput().getPermission();
                if (replacement.getOutput().hasColorRetention()
                        && context.getSender().hasPermission(permission))
                    suggestions.add(replacement.getId());
            }
        }

        return suggestions;
    }

}
