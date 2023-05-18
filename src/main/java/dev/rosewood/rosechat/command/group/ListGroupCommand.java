package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
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
        for (GroupChannel group : this.getAPI().getGroupChats()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-list-format",
                    StringPlaceholders.builder("group", group.getName())
                            .addPlaceholder("id", group.getId())
                            .addPlaceholder("owner", Bukkit.getOfflinePlayer(group.getOwner()).getName())
                            .addPlaceholder("members", group.getMembers().size()).build(), false);
        }
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
