package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
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
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        String color = args[0];

        if (!MessageUtils.canColor(sender, color, MessageLocation.CHATCOLOR.toString().toLowerCase())) return;

        String colorified = HexUtils.colorify(color);
        if (colorified.equals(color) || !ChatColor.stripColor(colorified).isEmpty() || color.contains("&r")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-color-invalid");
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
