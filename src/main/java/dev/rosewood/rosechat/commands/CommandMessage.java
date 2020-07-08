package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.floralapi.root.command.AbstractCommand;
import dev.rosewood.rosechat.floralapi.root.utils.Language;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandMessage extends AbstractCommand {

    public CommandMessage() {
        super("message", "msg", "m", "pm", "whisper", "w", "tell", "t");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Language.INVALID_ARGUMENTS.getLocalizedText().withPrefixPlaceholder()
                    .withPlaceholder("syntax", getSyntax()).sendMessage(sender);
            return;
        }

        if (args.length == 1) {
            new LocalizedText("enter-message").withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            Language.PLAYER_NOT_FOUND.getLocalizedText().withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        String message = getAllArgs(1, args);

        if (message.isEmpty()) {
            new LocalizedText("enter-message").withPrefixPlaceholder().sendMessage(sender);
            return;
        }

        // blegh, who tf usin console to send messages :angry:

        MessageWrapper messageSentWrapper = new MessageWrapper((Player) sender, message).parsePlaceholders("message-sent", target, (Player) sender);
        MessageWrapper messageReceivedWrapper = new MessageWrapper((Player) sender, message).parsePlaceholders("message-received", target, (Player) sender);
        sender.spigot().sendMessage(messageSentWrapper.build());
        target.spigot().sendMessage(messageReceivedWrapper.build());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) tab.add(player.getName());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.message";
    }

    @Override
    public String getSyntax() {
        return "/msg <player> <message>";
    }
}
