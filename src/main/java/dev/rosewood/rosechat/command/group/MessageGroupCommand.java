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

public class MessageGroupCommand extends AbstractCommand {

    public MessageGroupCommand() {
        super(true, "gcm", "gcmsg", "gmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-message-enter-message");
            return;
        }

        GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);

        if (groupChat == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
            return;
        }

        String message = getAllArgs(1, args);

        if (MessageUtils.isMessageEmpty(message)) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "message-blank");
            return;
        }

        RoseSender messageSender = new RoseSender((Player) sender);
        MessageWrapper messageWrapper = new MessageWrapper(messageSender, MessageLocation.GROUP, groupChat, message).validate().filter().applyDefaultColor();
        groupChat.send(messageWrapper);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (GroupChat groupChat : this.getAPI().getGroupChats(((Player) sender).getUniqueId())) {
                tab.add(groupChat.getId());
            }
        } else if (args.length == 2) {
            tab.add("<message>");
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.group.message";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-gcm-usage");
    }

}
