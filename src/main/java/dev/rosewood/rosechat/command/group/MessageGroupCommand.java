package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.message.MessageSender;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageGroupCommand extends AbstractCommand {

    public MessageGroupCommand() {
        super(true, "gcm", "gcmsg", "gmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-message-enter-message");
            return;
        }

        GroupChat groupChat = this.getAPI().getGroupChatById(args[0]);

        if (groupChat == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "gc-invalid");
            return;
        }

        String message = getAllArgs(1, args);

        String colorified = HexUtils.colorify(message);
        if (ChatColor.stripColor(colorified).isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "message-blank");
            return;
        }

        MessageSender messageSender = new MessageSender((Player) sender);
        MessageWrapper messageWrapper = new MessageWrapper(groupChat.getName(), messageSender, message);
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
