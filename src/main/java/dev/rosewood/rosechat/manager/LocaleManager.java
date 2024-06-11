package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractLocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class LocaleManager extends AbstractLocaleManager {

    public LocaleManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    public void sendComponentMessage(CommandSender sender, String messageKey, boolean prefix) {
        if (this.getMessage(messageKey).isEmpty())
            return;

        if (!prefix) {
            RosePlayer rosePlayer = new RosePlayer(sender);
            rosePlayer.send(RoseChatAPI.getInstance().parse(rosePlayer, rosePlayer, this.getMessage(messageKey)));
        } else {
            this.sendComponentMessage(sender, messageKey);
        }
    }

    public void sendComponentMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders, boolean prefix) {
        if (this.getMessage(messageKey).isEmpty())
            return;

        if (!prefix) {
            RosePlayer rosePlayer = new RosePlayer(sender);
            rosePlayer.send(RoseChatAPI.getInstance().parse(rosePlayer, rosePlayer, this.getMessage(messageKey, stringPlaceholders)));
        } else {
            this.sendComponentMessage(sender, messageKey, stringPlaceholders);
        }
    }

    public void sendComponentMessage(CommandSender sender, String messageKey) {
        if (this.getMessage(messageKey).isEmpty())
            return;

        this.sendComponentMessage(sender, messageKey, StringPlaceholders.empty());
    }

    public void sendComponentMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        if (this.getMessage(messageKey).isEmpty())
            return;

        RosePlayer rosePlayer = new RosePlayer(sender);
        this.sendComponentMessage(rosePlayer, messageKey, stringPlaceholders);
    }

    public void sendComponentMessage(RosePlayer sender, String messageKey) {
        if (this.getMessage(messageKey).isEmpty())
            return;

        this.sendComponentMessage(sender, messageKey, StringPlaceholders.empty());
    }

    public void sendComponentMessage(RosePlayer sender, String messageKey, StringPlaceholders stringPlaceholders) {
        if (this.getMessage(messageKey).isEmpty())
            return;

        sender.send(RoseChatAPI.getInstance().parse(sender, sender, this.getMessage("prefix") + this.getMessage(messageKey, stringPlaceholders)));
    }

    public String getMessage(String messageKey) {
        String message = this.getLocaleString(messageKey);
        return message == null ? ChatColor.RED + "Missing message in locale file: " + messageKey : message;
    }

    public String getMessage(String messageKey, StringPlaceholders stringPlaceholders) {
        String message = this.getLocaleString(messageKey);
        return message == null ? ChatColor.RED + "Missing message in locale file: " + messageKey : stringPlaceholders.apply(message);
    }

}
