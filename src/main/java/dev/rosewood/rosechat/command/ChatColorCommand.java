package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
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
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        String color = args[0];
        if (color.equalsIgnoreCase("remove")) {
            playerData.setColor("");
            playerData.save();
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-color-removed");
            return;
        }

        if (!MessageUtils.canColor(new RosePlayer(sender), color, MessageLocation.CHATCOLOR.toString().toLowerCase())) return;

        String colorified = HexUtils.colorify(color);
        if (colorified.equals(color) || !ChatColor.stripColor(colorified).isEmpty() || color.contains("&r")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-color-invalid");
            return;
        }

        String colorStr = color;
        colorStr = !colorStr.startsWith("<") ? colorStr.substring(1) : (colorStr.startsWith("<g") ?
                this.getAPI().getLocaleManager().getMessage("command-color-gradient") : colorStr.startsWith("<r:") ?
                this.getAPI().getLocaleManager().getMessage("command-color-rainbow") : colorStr);
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-color-success", StringPlaceholders.of("color", ChatColor.stripColor(color + colorStr)));
        playerData.setColor(color);
        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            tab.add("remove");
            if (sender.hasPermission("rosechat.color.chatcolor") && !Setting.USE_PER_COLOR_PERMISSIONS.getBoolean()) tab.add("&a");
            if (sender.hasPermission("rosechat.format.chatcolor")) tab.add("&l");
            if (sender.hasPermission("rosechat.hex.chatcolor")) tab.add("#FFFFFF");
            if (sender.hasPermission("rosechat.rainbow.chatcolor")) tab.add("<r:0.5>");
            if (sender.hasPermission("rosechat.gradient.chatcolor")) tab.add("<g:#FFFFFF:#000000>");
            if (Setting.USE_PER_COLOR_PERMISSIONS.getBoolean()) {
                for (ChatColor color : ChatColor.values()) {
                    if (sender.hasPermission("rosechat." + color.getName().toLowerCase() + ".chatcolor")) tab.add("&" + color.toString().substring(1));
                }
            }
        }

        return tab;
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
