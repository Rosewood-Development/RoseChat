package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.MessageSender;
import org.bukkit.entity.Player;

public enum FilterType {

    CAPS("blocked-caps"),
    SPAM("blocked-spam"),
    URL("blocked-url"),
    SWEAR("blocked-language");

    private final String warning;

    FilterType(String warning) {
        this.warning = warning;
    }

    /**
     * Sends a warning message, defined in the language file, to the sender.
     * @param sender The sender to receive the message.
     */
    public void sendWarning(MessageSender sender) {
        LocaleManager localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
        sender.send(localeManager.getLocaleMessage(this.warning));
    }

    /**
     * Sends a warning message, defined in the language file, to the player.
     * @param player The player to receive the message.
     */
    public void sendWarning(Player player) {
        LocaleManager localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
        player.sendMessage(localeManager.getLocaleMessage(this.warning));
    }
}
