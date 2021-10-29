package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupCommand extends AbstractCommand {

    public CreateGroupCommand() {
        super(true, "create");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player player = (Player) sender;
        if (this.getAPI().getGroupChatByOwner(player.getUniqueId()) != null) {
            this.getAPI().getLocaleManager().sendMessage(player, "command-gc-create-fail");
            return;
        }

        String id = args[0];
        String name = getAllArgs(1, args);
        RoseSender roseSender = new RoseSender(player);
        MessageWrapper message = new MessageWrapper(roseSender, MessageLocation.GROUP, null, name).validate().filterLanguage();
        if (!message.canBeSent()) {
            if (message.getFilterType() != null) message.getFilterType().sendWarning(roseSender);
            return;
        }

        GroupChat groupChat = this.getAPI().createGroupChat(id, player.getUniqueId());
        groupChat.setName(name);
        BaseComponent[] components = message.parse(null, roseSender);
        this.getAPI().getLocaleManager().sendMessage(player, "command-gc-create-success", StringPlaceholders.single("name", TextComponent.toLegacyText(components)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) tab.add("<id>");
        if (args.length == 2) tab.add("<display name>");
        return tab;
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
