package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.GroupManager.GroupInfo;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class GroupListCommand extends RoseChatCommand {

    public GroupListCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("list")
                .descriptionKey("command-gc-list-description")
                .permission("rosechat.group.list")
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RosePlayer player = new RosePlayer(context.getSender());
        GroupManager groupManager = this.getAPI().getGroupManager();

        player.sendLocaleMessage("command-gc-list-title");
        groupManager.getAllGroupInfo((infoList) -> {
            for (GroupInfo info : infoList) {
                player.sendLocaleMessage("command-gc-list-format",
                        StringPlaceholders.of(
                                "group", info.name(),
                                "id", info.id()
                        ));
            }

            player.sendLocaleMessage("command-gc-list-more");
        });
    }

}
