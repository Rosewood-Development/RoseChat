package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NickColorCommand extends AbstractCommand {

    public NickColorCommand() {
        super(true, "nickcolor", "nickcolour");
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
        if (color.equalsIgnoreCase("remove")) {
            if (playerData.getNickname() != null) {
                // Remove the players nickname if they only have a colour.
                if (ChatColor.stripColor(playerData.getNickname()).equalsIgnoreCase(sender.getName())) {
                    playerData.setNickname(null);
                    ((Player) sender).setDisplayName(null);
                } else {
                    // Remove only colours from the nickname.
                    String nickname = ChatColor.stripColor(HexUtils.colorify(player.getNickname())) + "&r";
                    playerData.setNickname(nickname);

                    RoseMessage nicknameMessage = new RoseMessage(player, MessageLocation.NICKNAME, nickname);
                    nicknameMessage.parse(player, null);

                    NicknameCommand.setDisplayName(player, nicknameMessage);
                }

                playerData.save();
            }
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickcolor-removed");
            return;
        }

        if (!MessageUtils.canColor(sender, color, MessageLocation.NICKNAME.toString().toLowerCase())) return;

        String colorified = HexUtils.colorify(color);
        if (colorified.equals(color) || !ChatColor.stripColor(colorified).isEmpty() || color.contains("&r")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickcolor-invalid");
            return;
        }

        String nickname;
        // Apply the colour to the player's name if they do not have a nickname.
        if (playerData.getNickname() == null) {
            nickname = color + sender.getName() + "&r";
            playerData.setNickname(nickname);
            NicknameCommand.setDisplayName((Player) sender, nickname);
        } else {
            // If the player already has a nickname, remove the colour and apply the new colour.
            nickname = ChatColor.stripColor(HexUtils.colorify(playerData.getNickname()));
            nickname = color + nickname + "&r";
            playerData.setNickname(nickname);
            NicknameCommand.setDisplayName((Player) sender, nickname);
        }

        playerData.save();

        String colorStr = color;
        colorStr = !colorStr.startsWith("<") ? colorStr.substring(1) : (colorStr.startsWith("<g") ?
                this.getAPI().getLocaleManager().getMessage("command-color-gradient") : colorStr.startsWith("<r:") ?
                this.getAPI().getLocaleManager().getMessage("command-color-rainbow") : colorStr);
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickcolor-success",
                StringPlaceholders.builder("color", ChatColor.stripColor(color + colorStr))
                        .addPlaceholder("name", nickname).build());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            tab.add("remove");
            if (sender.hasPermission("rosechat.color.nickname") && !Setting.USE_PER_COLOR_PERMISSIONS.getBoolean()) tab.add("&a");
            if (sender.hasPermission("rosechat.format.nickname")) tab.add("&l");
            if (sender.hasPermission("rosechat.hex.nickname")) tab.add("#FFFFFF");
            if (sender.hasPermission("rosechat.rainbow.nickname")) tab.add("<r:0.5>");
            if (sender.hasPermission("rosechat.gradient.nickname")) tab.add("<g:#FFFFFF:#000000>");
            if (Setting.USE_PER_COLOR_PERMISSIONS.getBoolean()) {
                for (ChatColor color : ChatColor.values()) {
                    if (sender.hasPermission("rosechat." + color.getName().toLowerCase() + ".nickname")) tab.add("&" + color.toString().substring(1));
                }
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.nickcolor";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-nickcolor-usage");
    }

}
