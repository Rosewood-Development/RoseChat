package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class SudoChatCommand extends AbstractCommand {

    public SudoChatCommand() {
        super(false, "sudo");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", this.getSyntax()));
            return;
        }

        String playerStr = args[0];
        Channel channel = this.getAPI().getChannelById(args[1]);

        if (channel == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
            return;
        }

        // Parse and remove the colour to get the player.
        String playerStrParsed = HexUtils.colorify(playerStr);
        playerStrParsed = ChatColor.stripColor(playerStrParsed);

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerStrParsed);
        RosePlayer sudoSender = (player == null || !player.isOnline()) ? new RosePlayer(playerStr, "default") : new RosePlayer(player.getPlayer());

        channel.send(sudoSender, getAllArgs(2, args));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != sender) tab.add(player.getName());
            }
        } else if (args.length == 2) {
            tab.addAll(this.getAPI().getChannelIDs());
        } else if (args.length == 3) {
            tab.add("<message>");
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.chat.sudo";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-sudo-usage");
    }

}
