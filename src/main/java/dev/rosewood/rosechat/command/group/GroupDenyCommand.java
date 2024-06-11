package dev.rosewood.rosechat.command.group;

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
import org.bukkit.OfflinePlayer;

public class GroupDenyCommand extends RoseChatCommand {

    public GroupDenyCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("deny")
                .descriptionKey("command-gc-deny-description")
                .permission("rosechat.group.deny")
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

        if (data.getGroupInvites().isEmpty()) {
            this.getLocaleManager().sendComponentMessage(player, "gc-no-invites");
            return;
        }

        this.execute(player, data.getGroupInvites().get(data.getGroupInvites().size() - 1));
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group) {
        RosePlayer player = new RosePlayer(context.getSender());
        this.execute(player, group);
    }

    private void execute(RosePlayer player, GroupChannel group) {
        PlayerData data = player.getPlayerData();
        data.getGroupInvites().remove(group);

        OfflinePlayer owner = Bukkit.getOfflinePlayer(group.getOwner());
        String name = player.getName();

        this.getLocaleManager().sendComponentMessage(player, "command-gc-deny-success",
                StringPlaceholders.of("name", group.getName()));

        if (owner.isOnline())
            this.getLocaleManager().sendComponentMessage(owner.getPlayer(), "command-gc-deny-denied",
                    StringPlaceholders.of("player", name));
    }

}
