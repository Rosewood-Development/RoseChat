package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.api.event.group.GroupNameEvent;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
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
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        GroupChannel groupChat = this.getAPI().getGroupChatById(args[0]);
        if (groupChat == null
                || (sender instanceof Player && !groupChat.getMembers().contains(((Player) sender).getUniqueId()) && !sender.hasPermission("rosechat.group.admin"))) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "gc-invalid");
            return;
        }

        String name = getAllArgs(1, args);
        RosePlayer rosePlayer = new RosePlayer(sender);

        if (!MessageUtils.canColor(rosePlayer, name, "group")) {
            rosePlayer.sendLocaleMessage("no-permission");
            return;
        }

        MessageRules messageRules = new MessageRules().applyLanguageFilter().applyCapsFilter().applyURLFilter();

        MessageRules.RuleOutputs outputs = messageRules.apply(rosePlayer, MessageLocation.GROUP, name);

        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null) outputs.getWarning().send(rosePlayer);
            return;
        }

        GroupNameEvent groupNameEvent = new GroupNameEvent(groupChat, name);
        Bukkit.getPluginManager().callEvent(groupNameEvent);
        if (groupNameEvent.isCancelled()) return;

        name = groupNameEvent.getName();

        groupChat.setName(name);
        groupChat.save();
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-gc-rename-success", StringPlaceholders.of("name", name));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("rosechat.group.admin")) {
                for (GroupChannel groupChat : this.getAPI().getGroupChats()) {
                    tab.add(groupChat.getId());
                }
            } else {
                GroupChannel groupChat = this.getAPI().getGroupChatByOwner(((Player) sender).getUniqueId());
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
