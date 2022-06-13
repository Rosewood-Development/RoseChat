package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class RenameGroupCommand extends AbstractCommand {

    public RenameGroupCommand() {
        super(false, "rename");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null
                || (sender instanceof Player && !groupChat.getMembers().contains(((Player) sender).getUniqueId()) && !sender.hasPermission("rosechat.group.admin"))) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
            return;
        }

        String name = getAllArgs(1, args);
        if (!MessageUtils.canColor(sender, name, "group")) return;

        RoseSender roseSender = new RoseSender(sender);
        MessageWrapper message = new MessageWrapper(roseSender, MessageLocation.GROUP, groupChat, name).validate().filterLanguage().filterCaps().filterURLs();

        if (!message.canBeSent()) {
            if (message.getFilterType() != null) message.getFilterType().sendWarning(roseSender);
            return;
        }

        // Reset colour & formatting so uncoloured names don't take colour from previous words.
        name = "&f&r" + message.parseToString() + "&f&r";
        groupChat.setName(name);
        groupChat.save();
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-rename-success", StringPlaceholders.single("name", name));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("rosechat.group.admin")) {
                for (GroupChat groupChat : this.getAPI().getGroupChats()) {
                    tab.add(groupChat.getId());
                }
            } else {
                GroupChat groupChat = this.getAPI().getGroupChatByOwner(((Player) sender).getUniqueId());
                if (groupChat != null) tab.add(groupChat.getId());
            }
        }

        if (args.length == 2) {
            tab.add("<name>");
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.rename";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gc-rename-usage");
    }

}
