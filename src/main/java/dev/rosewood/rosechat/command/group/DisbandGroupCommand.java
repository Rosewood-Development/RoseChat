package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisbandGroupCommand extends AbstractCommand {

    public DisbandGroupCommand() {
        super(false, "disband", "delete");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
                return;
            }

            Player player = (Player) sender;
            GroupChannel groupChat = this.getAPI().getGroupChatByOwner(player.getUniqueId());
            if (groupChat == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-gc");
                return;
            }

            for (UUID uuid : groupChat.getMembers()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    PlayerData data = this.getAPI().getPlayerData(uuid);
                    data.setCurrentChannel(Channel.findNextChannel(member));
                    this.getAPI().getLocaleManager().sendComponentMessage(member, "command-gc-disband-success", StringPlaceholders.of("name", groupChat.getName()));
                }
            }

            this.getAPI().deleteGroupChat(groupChat);
            return;
        }

        if (sender.hasPermission("rosechat.group.admin")) {
            GroupChannel groupChat = this.getAPI().getGroupChatById(args[0]);
            if (groupChat == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-does-not-exist");
                return;
            }

            for (UUID uuid : groupChat.getMembers()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    PlayerData data = this.getAPI().getPlayerData(uuid);
                    data.setCurrentChannel(Channel.findNextChannel(member));
                    groupChat.removeMember(uuid);
                    this.getAPI().getLocaleManager().sendComponentMessage(member, "command-gc-disband-success", StringPlaceholders.of("name", groupChat.getName()));
                }
            }

            this.getAPI().deleteGroupChat(groupChat);
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-disband-admin", StringPlaceholders.of("name", groupChat.getName()));
        } else {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-permission");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("rosechat.group.admin")) {
            for (GroupChannel groupChat : this.getAPI().getGroupChats()) {
                tab.add(groupChat.getId());
            }
        }
        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.disband";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-disband-usage");
    }

}
