package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
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
                this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
                return;
            }

            Player player = (Player) sender;
            GroupChat groupChat = this.getAPI().getGroupChatByOwner(player.getUniqueId());
            if (groupChat == null) {
                this.getAPI().getLocaleManager().sendMessage(sender, "no-gc");
                return;
            }

            RoseSender roseSender = new RoseSender(player);
            BaseComponent[] name = this.getAPI().parse(roseSender, roseSender, groupChat.getName());
            String formattedName = ComponentSerializer.toString(name);

            for (UUID uuid : groupChat.getMembers()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    this.getAPI().getLocaleManager().sendMessage(member, "command-gc-disband-success", StringPlaceholders.single("name", formattedName));
                }
            }

            this.getAPI().deleteGroupChat(groupChat);
            return;
        }

        if (sender.hasPermission("rosechat.group.admin")) {
            GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);
            if (groupChat == null) {
                this.getAPI().getLocaleManager().sendMessage(sender, "gc-does-not-exist");
                return;
            }

            RoseSender roseSender = new RoseSender(sender);
            BaseComponent[] name = this.getAPI().parse(roseSender, roseSender, groupChat.getName());
            String formattedName = ComponentSerializer.toString(name);

            for (UUID uuid : groupChat.getMembers()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    this.getAPI().getLocaleManager().sendMessage(member, "command-gc-disband-success", StringPlaceholders.single("name", formattedName));
                }
            }

            this.getAPI().deleteGroupChat(groupChat);
            this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-disband-admin", StringPlaceholders.single("name", formattedName));
        } else {
            this.getAPI().getLocaleManager().sendMessage(sender, "no-permission", StringPlaceholders.single("syntax", getSyntax()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("rosechat.group.admin")) {
            for (GroupChat groupChat : this.getAPI().getGroupChats()) {
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