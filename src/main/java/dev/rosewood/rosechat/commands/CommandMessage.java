package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.ConfigurationManager.Setting;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandMessage extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;

    public CommandMessage(RoseChat plugin) {
        super("message", "msg", "m", "pm", "whisper", "w", "tell", "t");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            localeManager.sendMessage(sender, "command-message-enter-message");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            localeManager.sendMessage(sender, "player-not-found");
            return;
        }

        String message = getAllArgs(1, args);

        if (message.isEmpty()) {
            localeManager.sendMessage(sender, "command-message-enter-message");
            return;
        }

        // TODO: Allow console sending & receiving messages.
        Player player = (Player) sender;

        MessageWrapper messageSentWrapper = new MessageWrapper(player, message).parsePlaceholders("message-sent", target);
        MessageWrapper messageReceivedWrapper = new MessageWrapper(player, message).parsePlaceholders("message-received", target);
        sender.spigot().sendMessage(messageSentWrapper.build());
        target.spigot().sendMessage(messageReceivedWrapper.build());

        try {
            Sound sound = Sound.valueOf(Setting.MESSAGE_SOUND.getString());
            target.playSound(target.getLocation(), sound, 1, 1);
        } catch (Exception e) {

        }

        DataManager dataManager = plugin.getManager(DataManager.class);
        dataManager.setLastMessagedBy(target, player);
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
        return localeManager.getLocaleMessage("command-message-usage");
    }
}
