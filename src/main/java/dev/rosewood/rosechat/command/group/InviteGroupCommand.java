package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class InviteGroupCommand extends AbstractCommand {

    public InviteGroupCommand() {
        super(true, "invite");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        GroupChat groupChat = this.getAPI().getGroupChatByOwner(player.getUniqueId());
        if (groupChat == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-gc");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }

        if (groupChat.getMembers().contains(target.getUniqueId())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-invite-member");
            return;
        }

        PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());

        String name = data.getNickname() == null ? player.getDisplayName() : data.getNickname();
        String targetName = targetData.getNickname() == null ? target.getDisplayName() : targetData.getNickname();

        this.getAPI().getLocaleManager().sendComponentMessage(target, "command-gc-invite-invited",
                StringPlaceholders.builder().addPlaceholder("player", name).addPlaceholder("name", groupChat.getName()).build());
        this.getAPI().getLocaleManager().sendComponentMessage(player, "command-gc-invite-success",
                StringPlaceholders.builder().addPlaceholder("player", targetName).addPlaceholder("name", groupChat.getName()).build());
        sendAcceptMessage(target, groupChat);

        targetData.inviteToGroup(groupChat);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != sender) tab.add(player.getName());
            }

            return tab;
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.invite";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-invite-usage");
    }

    private void sendAcceptMessage(Player player, GroupChat groupChat) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        componentBuilder.append("          ");
        componentBuilder.append(this.getAPI().getLocaleManager().getLocaleMessage("command-gc-accept-accept"))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gc accept " + groupChat.getId()))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(this.getAPI().getLocaleManager().getLocaleMessage("command-gc-accept-hover"))));
        componentBuilder.append("          ").retain(ComponentBuilder.FormatRetention.NONE);
        componentBuilder.append(this.getAPI().getLocaleManager().getLocaleMessage("command-gc-deny-deny"))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gc deny " + groupChat.getId()))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(this.getAPI().getLocaleManager().getLocaleMessage("command-gc-deny-hover"))));
        player.spigot().sendMessage(componentBuilder.create());
    }
}
