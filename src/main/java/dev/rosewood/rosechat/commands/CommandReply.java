package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;

import dev.rosewood.rosechat.managers.ConfigurationManager;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandReply extends AbstractCommand {

    private RoseChat plugin;
    private DataManager dataManager;
    private LocaleManager localeManager;

    public CommandReply(RoseChat plugin) {
        super("reply", "r");
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
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
        PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());

        if (playerData.getReplyTo() == null) {
            localeManager.sendMessage(sender, "command-reply-no-one");
            return;
        }

        Player target = Bukkit.getPlayer(playerData.getReplyTo());
        if (target == null) {
            localeManager.sendMessage(sender, "command-reply-no-one");
            return;
        }

        PlayerData targetData = dataManager.getPlayerData(target.getUniqueId());

        String message = getAllArgs(0, args);

        if (message.isEmpty()) {
            localeManager.sendMessage(sender, "command-reply-enter-message");
            return;
        }

        if (!playerData.canBeMessaged()) {
            localeManager.sendMessage(sender, "command-togglemessage-cannot-message");
            return;
        }

        MessageWrapper messageSentWrapper = new MessageWrapper(player, message)
                .checkAll()
                .filterAll()
                .withReplacements()
                .withTags().parsePlaceholders("message-sent", target);
        MessageWrapper messageReceivedWrapper = new MessageWrapper(target, message)
                .checkAll()
                .filterAll()
                .withReplacements()
                .withTags().parsePlaceholders("message-received", player);
        sender.spigot().sendMessage(messageSentWrapper.build());
        target.spigot().sendMessage(messageReceivedWrapper.build());
        targetData.setReplyTo(player.getUniqueId());

        MessageWrapper spyMessageWrapper = new MessageWrapper(player, message).checkAll()
                .filterAll()
                .withReplacements()
                .withTags().parsePlaceholders("social-spy", target);

        for (UUID uuid : dataManager.getSocialSpies()) {
            if (uuid.equals(((Player) sender).getUniqueId()) || uuid.equals(target.getUniqueId())) continue;
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null) spy.spigot().sendMessage(spyMessageWrapper.build());
        }

        try {
            if (targetData.hasMessageSounds()) {
                Sound sound = Sound.valueOf(ConfigurationManager.Setting.MESSAGE_SOUND.getString());
                target.playSound(target.getLocation(), sound, 1, 1);
            }
        } catch (Exception e) {

        }
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
