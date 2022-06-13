package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class InfoGroupCommand extends AbstractCommand {

    public InfoGroupCommand() {
        super(false, "info");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null
                || (sender instanceof Player && !groupChat.getMembers().contains(((Player) sender).getUniqueId()) && !sender.hasPermission("rosechat.group.admin"))) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
            return;
        }

        String ownerName = Bukkit.getPlayer(groupChat.getOwner()) == null ?
                Bukkit.getOfflinePlayer(groupChat.getOwner()).getName() : this.getAPI().getPlayerData(groupChat.getOwner()).getNickname();

        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-info-title", StringPlaceholders.single("group", groupChat.getName()), false);
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-info-format", StringPlaceholders.builder()
                .addPlaceholder("id", groupChat.getId())
                .addPlaceholder("owner", ownerName)
                .addPlaceholder("members", groupChat.getMembers().size()).build(), false);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("rosechat.group.admin")) {
                for (GroupChat groupChat : this.getAPI().getGroupChats()) {
                    tab.add(groupChat.getId());
                }
            } else {
                GroupChat groupChat = this.getAPI().getGroupChatByOwner(((Player) sender).getUniqueId());
                if (groupChat != null) tab.add(groupChat.getId());
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.info";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-info-usage");
    }

}
