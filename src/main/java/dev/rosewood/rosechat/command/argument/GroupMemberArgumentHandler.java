package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.List;

public class GroupMemberArgumentHandler extends ArgumentHandler<RosePlayer> {

    public GroupMemberArgumentHandler() {
        super(RosePlayer.class);
    }

    @Override
    public RosePlayer handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        Player player = MessageUtils.getPlayerExact(input);
        if (player == null) {
            throw new HandledArgumentException("argument-handler-player", StringPlaceholders.of("input", input));
        }

        GroupChannel group = context.get("group");
        if (group != null) {
            if (!group.getMembers().contains(player.getUniqueId()))
                throw new HandledArgumentException("argument-handler-group-member", StringPlaceholders.of("input", input));
            else
                return new RosePlayer(player);
        }

        RosePlayer sender = new RosePlayer(context.getSender());
        group = sender.getOwnedGroupChannel();
        if (!group.getMembers().contains(player.getUniqueId()))
            throw new HandledArgumentException("argument-handler-group-member", StringPlaceholders.of("input", input));
        else
            return new RosePlayer(player);
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        GroupChannel group = context.get("group");
        if (group != null) {
            return group.getMembers().stream().map(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                return player == null ? Bukkit.getOfflinePlayer(uuid).getName() : ChatColor.stripColor(player.getDisplayName());
            }).toList();
        }

        RosePlayer sender = new RosePlayer(context.getSender());
        group = sender.getOwnedGroupChannel();
        return group.getMembers().stream().map(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            return player == null ? Bukkit.getOfflinePlayer(uuid).getName() : ChatColor.stripColor(player.getDisplayName());
        }).toList();
    }

}
