package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

public class ListGroupCommand extends AbstractCommand {

    public ListGroupCommand() {
        super(false, "list");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-list-title", false);

        GroupManager groupManager = this.getAPI().getGroupManager();
        groupManager.getAllGroupInfo((infoList) -> {
            for (GroupManager.GroupInfo info : infoList) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-list-format",
                        StringPlaceholders.builder("group", info.name())
                                .add("id",info.id()).build(), false);
            }

            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-list-more", false);
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return "rosechat.group.list";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-list-usage");
    }

}
