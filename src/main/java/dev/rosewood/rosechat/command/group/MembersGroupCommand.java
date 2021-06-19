package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MembersGroupCommand extends AbstractCommand {

    public MembersGroupCommand() {
        super(false, "members");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null
                || (sender instanceof Player && !groupChat.getMembers().contains(((Player) sender).getUniqueId()) && !sender.hasPermission("rosechat.group.admin"))) {
            this.getAPI().getLocaleManager().sendMessage(sender, "gc-invalid");
            return;
        }

        this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-members-title", StringPlaceholders.single("name", groupChat.getName()));
        for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            String name = member == null ? Bukkit.getOfflinePlayer(uuid).getName() : member.getDisplayName();
            if (uuid.equals(groupChat.getOwner())) name = HexUtils.colorify("&e✵ &b" + name);
            this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-members-member", StringPlaceholders.single("player", name));
        }
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
                for (GroupChat groupChat : this.getAPI().getGroupChats(((Player) sender).getUniqueId())) {
                    tab.add(groupChat.getId());
                }
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.members";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-members-usage");
    }
}
