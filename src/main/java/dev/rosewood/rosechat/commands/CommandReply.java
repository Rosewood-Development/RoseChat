package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import dev.rosewood.rosechat.floralapi.root.utils.Language;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandReply extends AbstractCommand {

    private RoseChat plugin;

    public CommandReply(RoseChat plugin) {
        super("reply", "r");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Language.PLAYER_ONLY.getLocalizedText().withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        if (args.length == 0) {
            new LocalizedText("enter-message").withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        Player player = (Player) sender;
        if (plugin.getDataManager().getLastMessagedBy(player) == null) {
            new LocalizedText("no-reply").withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(plugin.getDataManager().getLastMessagedBy(player));
        if (target == null) {
            new LocalizedText("no-reply").withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        String message = getAllArgs(0, args);

        if (message.isEmpty()) {
            new LocalizedText("enter-message").withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        MessageWrapper messageSentWrapper = new MessageWrapper(player, message).parsePlaceholders("message-sent", target, player);
        MessageWrapper messageReceivedWrapper = new MessageWrapper(player, message).parsePlaceholders("message-received", target, player);
        sender.spigot().sendMessage(messageSentWrapper.build());
        target.spigot().sendMessage(messageReceivedWrapper.build());
        plugin.getDataManager().setLastMessagedBy(target, player);
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
        return "/reply <player>";
    }
}
