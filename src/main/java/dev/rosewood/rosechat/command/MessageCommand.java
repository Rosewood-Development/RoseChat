package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MessageCommand extends AbstractCommand {

    public MessageCommand() {
        super(false, "message", "msg", "m", "pm", "whisper", "w", "tell", "t");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-message-enter-message");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "player-not-found");
            return;
        }

        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());
        String message = getAllArgs(1, args);

        if (message.isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-message-enter-message");
            return;
        }

        if (!targetData.canBeMessaged()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-togglemessage-cannot-message");
            return;
        }

        String colorified = HexUtils.colorify(message);
        if (ChatColor.stripColor(colorified).isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "message-blank");
            return;
        }

        MessageUtils.sendPrivateMessage(sender, target, message);

        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
            playerData.setReplyTo(target.getUniqueId());
            targetData.setReplyTo(player.getUniqueId());
            playerData.save();
            targetData.save();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()){
                if (sender instanceof Player && player.getUniqueId().equals(((Player) sender).getUniqueId())) continue;
                tab.add(player.getName());
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.message";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-message-usage");
    }
}
