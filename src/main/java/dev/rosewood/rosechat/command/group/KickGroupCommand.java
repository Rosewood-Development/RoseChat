package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
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
        super(true, "kick", "remove");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        GroupChannel groupChat = this.getAPI().getGroupChatByOwner(player.getUniqueId());
        if (groupChat == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-gc");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (args[0].equalsIgnoreCase(player.getName())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-kick-self");
            return;
        }

        /*if (target == null || !groupChat.getMembers().contains(target.getUniqueId())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-kick-invalid-player");
            return;
        }*/

        PlayerData data = this.getAPI().getPlayerData(target.getUniqueId());
        String name = data.getNickname() == null ? target.getName() : data.getNickname();

        if (target.isOnline()) {
            Player kicked = Bukkit.getPlayer(target.getUniqueId());
            if (kicked != null) {
                this.getAPI().getLocaleManager().sendComponentMessage(kicked, "command-gc-kick-kicked", StringPlaceholders.single("name", groupChat.getName()));
            }
        }

        //groupChat.removeMember(target.getUniqueId());
        //this.getAPI().getGroupManager().removeMember(groupChat, target.getUniqueId());

       /* for (UUID uuid : groupChat.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null){
                this.getAPI().getLocaleManager().sendComponentMessage(member, "command-gc-kick-success",
                        StringPlaceholders.builder("player", name)
                                .addPlaceholder("name", groupChat.getName())
                                .build());
            }
        }*/
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            GroupChannel groupChat = this.getAPI().getGroupChatByOwner(((Player) sender).getUniqueId());
            if (groupChat != null) {
                /*for (UUID uuid : groupChat.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && member != sender) tab.add(member.getName());
                }*/
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
