package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.message.RoseSender;
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
     * @param sender The person to receive the message.
     */
    public void sendWarning(RoseSender sender) {
        sender.sendLocaleMessage(this.warning);
    }

    /**
     * Sends a warning message, defined in the language file, to the player.
     * @param player The person to receive the message.
     */
    public void sendWarning(Player player) {
        RoseSender sender = new RoseSender(player);
        this.sendWarning(sender);
    }

}
