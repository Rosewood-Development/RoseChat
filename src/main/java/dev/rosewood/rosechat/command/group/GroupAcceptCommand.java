package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.api.event.group.GroupJoinEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.UUID;

public class GroupAcceptCommand extends RoseChatCommand {

    public GroupAcceptCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("accept")
                .aliases("join")
                .descriptionKey("command-gc-accept-description")
                .permission("rosechat.group.accept")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .optional("group", RoseChatArgumentHandlers.GROUP_INVITE)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RosePlayer player = new RosePlayer(context.getSender());
        PlayerData data = player.getPlayerData();
        this.execute(player, data.getGroupInvites().get(data.getGroupInvites().size() - 1));
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group) {
        this.execute(new RosePlayer(context.getSender()), group);
    }

    public void execute(RosePlayer player, GroupChannel group) {
        PlayerData data = player.getPlayerData();

        int currentGroupCount = this.getAPI().getGroupChats(player.getUUID()).size();
        int maxAmount = 1;
        for (PermissionAttachmentInfo info : player.asPlayer().getEffectivePermissions()) {
            String target = info.getPermission().toLowerCase();
            if (target.startsWith("rosechat.groups.") && info.getValue()) {
                try {
                    maxAmount = Math.max(maxAmount, Integer.parseInt(target.substring(target.lastIndexOf(".") + 1)));
                } catch (NumberFormatException ignored) {

                }
            }
        }

        if (currentGroupCount >= maxAmount) {
            this.getLocaleManager().sendComponentMessage(player, "gc-limit");
            return;
        }

        data.getGroupInvites().remove(group);

        GroupJoinEvent groupJoinEvent = new GroupJoinEvent(group, player.asPlayer());
        Bukkit.getPluginManager().callEvent(groupJoinEvent);
        if (groupJoinEvent.isCancelled())
            return;

        String name = player.getName();

        for (UUID uuid : group.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null)
                this.getLocaleManager().sendComponentMessage(member, "command-gc-accept-accepted",
                        StringPlaceholders.of(
                                "name", group.getName(),
                                "player", name
                        ));
        }

        this.getLocaleManager().sendComponentMessage(player, "command-gc-accept-success",
                StringPlaceholders.of(
                        "name", group.getName(),
                        "player", name
                ));

        group.join(player.getUUID());
    }

}
