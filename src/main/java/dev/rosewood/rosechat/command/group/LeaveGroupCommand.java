package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.api.event.group.GroupLeaveEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
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
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        GroupChannel groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null || !groupChat.getMembers().contains(player.getUniqueId())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
            return;
        }

        if (groupChat.getOwner() == player.getUniqueId()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-leave-own");
            return;
        }

        GroupLeaveEvent groupLeaveEvent = new GroupLeaveEvent(groupChat, player);
        Bukkit.getPluginManager().callEvent(groupLeaveEvent);
        if (groupLeaveEvent.isCancelled()) return;

        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
        String name = data.getNickname() == null ? player.getDisplayName() : data.getNickname();

        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-leave-success", StringPlaceholders.of("name", groupChat.getName()));

        groupChat.removeMember(player.getUniqueId());
        this.getAPI().getGroupManager().removeMember(groupChat, player.getUniqueId());

        for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) {
                this.getAPI().getLocaleManager().sendComponentMessage(member, "command-gc-leave-left",
                        StringPlaceholders.builder("player", name)
                                .add("name", groupChat.getName())
                                .build());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (GroupChannel groupChat : this.getAPI().getGroupChats(((Player) sender).getUniqueId())) {
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
