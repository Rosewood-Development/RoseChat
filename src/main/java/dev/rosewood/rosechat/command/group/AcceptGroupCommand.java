package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
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
            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-accept-no-invites");
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

            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-accept-not-invited");
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
        int currentGroups = (int) this.getAPI().getGroupChats().stream().filter(gc -> (gc.getMembers().contains(player.getUniqueId()))).count();

        int amount = 1;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String target = info.getPermission().toLowerCase();
            if (target.startsWith("rosechat.groups.") && info.getValue()) {
                try {
                    amount = Math.max(amount, Integer.parseInt(target.substring(target.lastIndexOf(".") + 1)));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (currentGroups >= amount) {
            this.getAPI().getLocaleManager().sendComponentMessage(player, "gc-limit");
            return;
        }

        data.getGroupInvites().remove(groupChat);

        String name = data.getNickname() == null ? player.getDisplayName() : data.getNickname();

        for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null)
                this.getAPI().getLocaleManager().sendComponentMessage(member, "command-gc-accept-accepted",
                            StringPlaceholders.builder().addPlaceholder("name", groupChat.getName()).addPlaceholder("player", name).build());
        }

        this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-accept-success",
                StringPlaceholders.builder().addPlaceholder("name", groupChat.getName()).addPlaceholder("player", name).build());

        this.getAPI().addGroupChatMember(groupChat, player);
    }

}
