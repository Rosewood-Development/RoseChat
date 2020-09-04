package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.command.CommandSender;

public enum FilterType {
    CAPS("blocked-caps"),
    SPAM("blocked-spam"),
    URL("blocked-url"),
    SWEAR("blocked-language");

    private String warning;

    FilterType(String warning) {
        this.warning = warning;
    }

    public void sendWarning(CommandSender sender) {
        LocaleManager localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
        localeManager.sendMessage(sender, this.warning);
    }
}
