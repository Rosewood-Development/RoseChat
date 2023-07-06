package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfoGroupCommand extends AbstractCommand {

    public InfoGroupCommand() {
        super(false, "info");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        GroupChannel groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null) {
            GroupManager groupManager = this.getAPI().getGroupManager();
            groupManager.getGroupInfo(args[0], info -> {
                if (info != null) {
                    if (info.owner() == null) {
                        this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-does-not-exist");
                        return;
                    }

                    String owner = Bukkit.getOfflinePlayer(UUID.fromString(info.owner())).getName();

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (player.hasPermission("rosechat.group.admin")) {
                            this.sendInfoMessage(sender, info.id(), info.name(), owner, info.members());
                        } else {
                            boolean isInGroup = false;
                            for (GroupChannel gc : this.getAPI().getGroupChats(player.getUniqueId()))    {
                                if (gc.getId().equalsIgnoreCase(info.id())) {
                                    this.sendInfoMessage(sender, info.id(), info.name(), owner, info.members());
                                    isInGroup = true;
                                }
                            }

                            if (!isInGroup) {
                                this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
                            }
                        }
                    } else {
                        this.sendInfoMessage(sender, info.id(), info.name(), owner, info.members());
                    }
                } else {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-does-not-exist");
                }
            });
        } else {
            if (sender instanceof Player && !groupChat.getMembers().contains(((Player) sender).getUniqueId()) && !sender.hasPermission("rosechat.group.admin")) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
                return;
            }

            PlayerData playerData = this.getAPI().getPlayerData(groupChat.getOwner());
            String nickname = playerData == null ? null : playerData.getNickname();
            String ownerName = Bukkit.getPlayer(groupChat.getOwner()) == null || nickname == null ?
                    Bukkit.getOfflinePlayer(groupChat.getOwner()).getName() : nickname;

            this.sendInfoMessage(sender, groupChat.getId(), groupChat.getName(), ownerName, groupChat.getMembers().size());
        }
    }

    public void sendInfoMessage(CommandSender sender, String id, String name, String owner, int members) {
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-info-title", StringPlaceholders.of("group", name), false);
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-info-format", StringPlaceholders.builder()
                .addPlaceholder("id", id)
                .addPlaceholder("owner", owner)
                .addPlaceholder("members", members).build(), false);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("rosechat.group.admin")) {
                for (GroupChannel groupChat : this.getAPI().getGroupChats()) {
                    tab.add(groupChat.getId());
                }
                tab.addAll(this.getAPI().getGroupChatNames());
            } else {
                GroupChannel groupChat = this.getAPI().getGroupChatByOwner(((Player) sender).getUniqueId());
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
