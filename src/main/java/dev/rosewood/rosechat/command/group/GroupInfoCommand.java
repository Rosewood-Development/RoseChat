package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.GroupManager.GroupInfo;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;

import java.util.UUID;

public class GroupInfoCommand extends RoseChatCommand {

    public GroupInfoCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("info")
                .descriptionKey("command-gc-info-description")
                .permission("rosechat.group.info")
                .arguments(ArgumentsDefinition.builder()
                        .optional("group", RoseChatArgumentHandlers.OFFLINE_GROUP,
                                ArgumentCondition.hasPermission("rosechat.group.admin"))
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RosePlayer player = new RosePlayer(context.getSender());
        GroupChannel group = player.getOwnedGroupChannel();
        if (group == null) {
            player.sendLocaleMessage("no-gc");
            return;
        }

        GroupInfo info = new GroupInfo(group.getId(),
                group.getName(),
                group.getOwner().toString(),
                group.getMembers().size());
        this.execute(player, info);
    }

    @RoseExecutable
    public void execute(CommandContext context, String group) {
        GroupManager groupManager = this.getAPI().getGroupManager();
        groupManager.getGroupInfo(group, (info) -> this.execute(new RosePlayer(context.getSender()), info));
    }

    private void execute(RosePlayer player, GroupInfo info) {
        player.sendLocaleMessage("command-gc-info-title",
                StringPlaceholders.of("group", info.name()));
        player.sendLocaleMessage("command-gc-info-format",
                StringPlaceholders.of(
                        "id", info.id(),
                        "owner", Bukkit.getOfflinePlayer(UUID.fromString(info.owner())).getName(),
                        "members", info.members()
                ));
    }

}
