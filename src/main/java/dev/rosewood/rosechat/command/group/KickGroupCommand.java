package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KickGroupCommand extends AbstractCommand {

    public KickGroupCommand() {
        super(true, "kick");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        GroupChat groupChat = this.getAPI().getGroupChatByOwner(player.getUniqueId());
        if (groupChat == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "no-gc");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (args[0].equalsIgnoreCase(player.getName())) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-kick-self");
            return;
        }

        if (target == null || !groupChat.getMembers().contains(target.getUniqueId())) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-gc-kick-invalid-player");
            return;
        }

        if (target.isOnline()) {
            Player kicked = Bukkit.getPlayer(target.getUniqueId());
            if (kicked != null) {
                this.getAPI().getLocaleManager().sendMessage(kicked, "command-gc-kick-kicked", StringPlaceholders.single("name", groupChat.getName()));
            }
        }

        groupChat.removeMember(target.getUniqueId());
        this.getAPI().getGroupManager().removeMember(groupChat, target.getUniqueId());

        for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null){
                this.getAPI().getLocaleManager().sendMessage(member, "command-gc-kick-success",
                        StringPlaceholders.builder("player", target.getName())
                                .addPlaceholder("name", groupChat.getName())
                                .build());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            GroupChat groupChat = this.getAPI().getGroupChatByOwner(((Player) sender).getUniqueId());
            if (groupChat != null) {
                for (UUID uuid : groupChat.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && member != sender) tab.add(member.getName());
                }
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.kick";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-kick-usage");
    }
}
