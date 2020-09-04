package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.floralapi.AbstractCommand;

import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandReply extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;

    public CommandReply(RoseChat plugin) {
        super("reply", "r");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // TODO: Maybe allow the console to reply?

        if (!(sender instanceof Player)) {
            localeManager.sendMessage(sender, "player-only");
            return;
        }

        if (args.length == 0) {
            localeManager.sendMessage(sender, "command-reply-enter-message");
            return;
        }

        Player player = (Player) sender;
        DataManager dataManager = plugin.getManager(DataManager.class);

        if (dataManager.getLastMessagedBy(player) == null) {
            localeManager.sendMessage(sender, "command-reply-no-one");
            return;
        }

        Player target = Bukkit.getPlayer(dataManager.getLastMessagedBy(player));
        if (target == null) {
            localeManager.sendMessage(sender, "command-reply-no-one");
            return;
        }

        String message = getAllArgs(0, args);

        if (message.isEmpty()) {
            localeManager.sendMessage(sender, "command-reply-enter-message");
            return;
        }

        MessageWrapper messageSentWrapper = new MessageWrapper(player, message).parsePlaceholders("message-sent", player);
        MessageWrapper messageReceivedWrapper = new MessageWrapper(player, message).parsePlaceholders("message-received", player);
        sender.spigot().sendMessage(messageSentWrapper.build());
        target.spigot().sendMessage(messageReceivedWrapper.build());
        dataManager.setLastMessagedBy(target, player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.reply";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-reply-usage");
    }
}
