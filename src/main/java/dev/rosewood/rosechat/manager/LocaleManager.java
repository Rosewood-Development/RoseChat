package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.locale.EnglishLocale;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosegarden.manager.AbstractLocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

public class LocaleManager extends AbstractLocaleManager {

    public LocaleManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public List<Locale> getLocales() {
        return Collections.singletonList(new EnglishLocale());
    }

    public void sendComponentMessage(CommandSender sender, String messageKey) {
        this.sendComponentMessage(sender, messageKey, StringPlaceholders.empty());
    }

    public void sendComponentMessage(CommandSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        RoseSender roseSender = new RoseSender(sender);
        this.sendComponentMessage(roseSender, messageKey, stringPlaceholders);
    }

    public void sendComponentMessage(RoseSender sender, String messageKey) {
        this.sendComponentMessage(sender, messageKey, StringPlaceholders.empty());
    }

    public void sendComponentMessage(RoseSender sender, String messageKey, StringPlaceholders stringPlaceholders) {
        sender.send(RoseChatAPI.getInstance().parse(sender, sender, this.getMessage("prefix") + this.getMessage(messageKey, stringPlaceholders)));
    }

    public String getMessage(String messageKey) {
        String message = this.locale.getString(messageKey);
        return message == null ? ChatColor.RED + "Missing message in locale file: " + messageKey : message;
    }

    public String getMessage(String messageKey, StringPlaceholders stringPlaceholders) {
        String message = this.locale.getString(messageKey);
        return message == null ? ChatColor.RED + "Missing message in locale file: " + messageKey : stringPlaceholders.apply(message);
    }
}
