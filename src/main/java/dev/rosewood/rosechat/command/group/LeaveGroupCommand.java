package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
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

public class LeaveGroupCommand extends AbstractCommand {

    public LeaveGroupCommand() {
        super(true, "leave");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null || !groupChat.getMembers().contains(player.getUniqueId())) {
            this.getAPI().getLocaleManager().sendMessage(sender, "gc-invalid");
            return;
        }

        if (groupChat.getOwner() == player.getUniqueId()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-leave-own");
            return;
        }

        RoseSender roseSender = new RoseSender(player);
        BaseComponent[] groupName = this.getAPI().parse(roseSender, roseSender, groupChat.getName());
        String formattedGroupName = ComponentSerializer.toString(groupName);

        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
        BaseComponent[] name = this.getAPI().parse(roseSender, roseSender, data.getNickname() == null ? player.getDisplayName() : data.getNickname());
        String formattedName = ComponentSerializer.toString(name);

        this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-leave-success", StringPlaceholders.single("name", formattedGroupName));

        groupChat.removeMember(player);
        this.getAPI().getGroupManager().removeMember(groupChat, player.getUniqueId());

        for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) {
                this.getAPI().getLocaleManager().sendMessage(member, "command-gc-leave-left",
                        StringPlaceholders.builder("player", formattedName)
                                .addPlaceholder("name", formattedGroupName)
                                .build());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (GroupChat groupChat : this.getAPI().getGroupChats(((Player) sender).getUniqueId())) {
                tab.add(groupChat.getId());
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.leave";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-leave-usage");
    }
}
