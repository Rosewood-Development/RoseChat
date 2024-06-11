package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
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
                context.getSender().hasPermission("rosechat.color." + this.permissionArea) && !Setting.USE_PER_COLOR_PERMISSIONS.getBoolean())
            suggestions.add("&a");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.format." + this.permissionArea))
            suggestions.add("&l");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.hex." + this.permissionArea))
            suggestions.add("#FFFFFF");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.rainbow." + this.permissionArea))
            suggestions.add("<r:0.5>");

        if (this.permissionArea == null ||
                context.getSender().hasPermission("rosechat.gradient." + this.permissionArea))
            suggestions.add("<g:#FFFFFF:#000000>");

        return suggestions;
    }

}
