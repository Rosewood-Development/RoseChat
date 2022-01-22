package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Matcher colorMatcher = ComponentColorizer.VALID_LEGACY_REGEX.matcher(color);
        Matcher formatMatcher = ComponentColorizer.VALID_LEGACY_REGEX_FORMATTING.matcher(color);
        Matcher hexMatcher = ComponentColorizer.HEX_REGEX.matcher(color);
        Matcher gradientMatcher = ComponentColorizer.GRADIENT_PATTERN.matcher(color);
        Matcher rainbowMatcher = ComponentColorizer.RAINBOW_PATTERN.matcher(color);

        boolean canColor = !colorMatcher.find() || sender.hasPermission("rosechat.color.chatcolor");
        boolean canMagic = !color.contains("&k") || sender.hasPermission("rosechat.magic.chatcolor");
        boolean canFormat = !formatMatcher.find() || sender.hasPermission("rosechat.format.chatcolor");
        boolean canHex = !hexMatcher.find() || sender.hasPermission("rosechat.hex.chatcolor");
        boolean canGradient = !gradientMatcher.find() || sender.hasPermission("rosechat.gradient.chatcolor");
        boolean canRainbow = !rainbowMatcher.find() || sender.hasPermission("rosechat.rainbow.chatcolor");

        if (!(canColor && canMagic && canFormat && canHex && canGradient && canRainbow)) {
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
