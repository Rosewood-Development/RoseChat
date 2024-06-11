package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.GroupArgumentHandler;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.UUID;

public class GroupMembersCommand extends RoseChatCommand {

    public GroupMembersCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("members")
                .descriptionKey("command-gc-members-description")
                .permission("rosechat.group.members")
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

        this.execute(player, group);
    }

    @RoseExecutable
    public void execute(CommandContext context, String groupId) {
        GroupManager groupManager = this.getAPI().getGroupManager();
        groupManager.loadGroupChat(groupId, (group) -> this.execute(new RosePlayer(context.getSender()), group));
    }

    private void execute(RosePlayer player, GroupChannel group) {
        player.sendLocaleMessage("command-gc-members-title",
                StringPlaceholders.of("name", group.getName()));
        for (UUID uuid : group.getMembers()) {
            OfflinePlayer offlineMember = Bukkit.getOfflinePlayer(uuid);

            String name;
            if (offlineMember.isOnline()) {
                RosePlayer member = new RosePlayer(offlineMember.getPlayer());
                name = member.getName();
            } else
                name = offlineMember.getName();

            String key = uuid.equals(group.getOwner()) ? "command-gc-members-owner" : "command-gc-members-member";
            player.sendLocaleMessage(key,
                    StringPlaceholders.of("player", name));
        }
    }

}
