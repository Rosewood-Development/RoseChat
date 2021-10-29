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

public class AcceptGroupCommand extends AbstractCommand {

    public AcceptGroupCommand() {
        super(true, "accept", "join");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());

        if (playerData.getGroupInvites().isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(player, "command-gc-accept-no-invites");
            return;
        }

        if (args.length == 0) {
            GroupChat invite = playerData.getGroupInvites().get(playerData.getGroupInvites().size() - 1);
            this.accept(playerData, player, invite);
        } else {
            String id = args[0];

            for (GroupChat invite : playerData.getGroupInvites()) {
                if (invite.getId().equalsIgnoreCase(id)) {
                    this.accept(playerData, player, invite);
                    return;
                }
            }

            this.getAPI().getLocaleManager().sendMessage(player, "command-gc-accept-not-invited");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            if (sender instanceof Player) {
                for (GroupChat groupChat : this.getAPI().getPlayerData(((Player) sender).getUniqueId()).getGroupInvites()) {
                    tab.add(groupChat.getId());
                }
            }

            return tab;
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.accept";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-group-accept-usage");
    }

    private void accept(PlayerData data, Player player, GroupChat groupChat) {
        data.getGroupInvites().remove(groupChat);
        this.getAPI().addGroupChatMember(groupChat, player);

        RoseSender roseSender = new RoseSender(player);
        BaseComponent[] groupName = this.getAPI().parse(roseSender, roseSender, groupChat.getName());
        String formattedGroupName = ComponentSerializer.toString(groupName);

        BaseComponent[] name = this.getAPI().parse(roseSender, roseSender, data.getNickname() == null ? player.getDisplayName() : data.getNickname());
        String formattedName = ComponentSerializer.toString(name);

        this.getAPI().getLocaleManager().sendMessage(player, "some-msg", StringPlaceholders.single("player_name", player.getName()));

        this.getAPI().getLocaleManager().sendMessage(player, "command-gc-accept-success",
                StringPlaceholders.builder().addPlaceholder("name", formattedGroupName).addPlaceholder("player", formattedGroupName).build());

        for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) this.getAPI().getLocaleManager()
                    .sendMessage(member, "command-gc-accept-accepted",
                            StringPlaceholders.builder().addPlaceholder("name", formattedGroupName).addPlaceholder("player", formattedName).build());
        }
    }
}
