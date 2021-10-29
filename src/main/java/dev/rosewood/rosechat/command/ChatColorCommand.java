package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatColorCommand extends AbstractCommand {

    public ChatColorCommand() {
        super(true, "color", "chatcolor");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = this.getAPI().getPlayerData(uuid);

        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        String color = args[0];
        if (!sender.hasPermission("rosechat.color.chatcolor") && color.matches(ComponentColorizer.VALID_LEGACY_REGEX.pattern())
                || (!sender.hasPermission("rosechat.magic.chatcolor") && color.equalsIgnoreCase("&k"))
                || (!sender.hasPermission("rosechat.format.chatcolor") && color.matches(ComponentColorizer.VALID_LEGACY_REGEX_FORMATTING.pattern()))
                || (!sender.hasPermission("rosechat.hex.chatcolor") && color.matches(ComponentColorizer.HEX_REGEX.pattern()))
                || (!sender.hasPermission("rosechat.gradient.chatcolor") && color.matches(ComponentColorizer.GRADIENT_PATTERN.pattern()))
                || (!sender.hasPermission("rosechat.rainbow.chatcolor") && color.matches(ComponentColorizer.RAINBOW_PATTERN.pattern()))) {
            this.getAPI().getLocaleManager().sendMessage(sender, "no-permission");
            return;
        }

        String colorified = HexUtils.colorify(color);
        if (colorified.equals(color) || !ChatColor.stripColor(colorified).isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-color-invalid");
            return;
        }

        String colorStr = color;
        colorStr = !colorStr.startsWith("<") ? colorStr.substring(1) : colorStr;
        this.getAPI().getLocaleManager().sendMessage(sender, "command-color-success", StringPlaceholders.single("color", ChatColor.stripColor(color + colorStr)));
        playerData.setColor(color);
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        return "rosechat.chatcolor";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-color-usage");
    }
}
