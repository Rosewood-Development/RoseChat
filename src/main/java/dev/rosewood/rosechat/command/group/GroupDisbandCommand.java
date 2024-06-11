package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class GroupDisbandCommand extends RoseChatCommand {

    public GroupDisbandCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("disband")
                .aliases("delete")
                .descriptionKey("command-gc-disband-description")
                .permission("rosechat.group.disband")
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
    public void execute(CommandContext context, String id) {
        this.getAPI().getGroupManager().loadGroupChat(id, (group) -> {
            this.execute(new RosePlayer(context.getSender()), group);
        });
    }

    private void execute(RosePlayer player, GroupChannel group) {
        boolean success = group.disband();
        if (!success)
            return;

        List<UUID> members = group.getMembers();
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (member == null)
                continue;

            PlayerData data = this.getAPI().getPlayerData(member.getUniqueId());
            if (data.getCurrentChannel() == group)
                data.setCurrentChannel(Channel.findNextChannel(member));

            this.getLocaleManager().sendComponentMessage(member, "command-gc-disband-success",
                    StringPlaceholders.of("name", group.getName()));
        }

        if (player.hasPermission("rosechat.group.admin"))
            player.sendLocaleMessage("command-gc-disband-admin",
                    StringPlaceholders.of("name", group.getName()));
    }

}
