package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class CreateGroupCommand extends AbstractCommand {

    public CreateGroupCommand() {
        super(true, "create");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        String name = getAllArgs(1, args);
        GroupChat groupChat = this.getAPI().createGroupChat(player.getUniqueId());
        groupChat.setName(name);

        //groupManager.addGroupChat(groupChat);

        // add group chat, add player
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.create";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-create-usage");
    }
}