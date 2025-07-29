package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.entity.Player;

public enum FilterWarning {

    CAPS("blocked-caps"),
    SPAM("blocked-spam"),
    URL("blocked-url"),
    SWEAR("blocked-language");

    private final String warning;

    FilterWarning(String warning) {
        this.warning = warning;
    }

    /**
     * Sends a warning message, defined in the language file, to the sender.
     * @param sender The person to receive the message.
     */
    public void send(RosePlayer sender) {
        sender.sendLocaleMessage(this.warning);
    }

    /**
     * Sends a warning message, defined in the language file, to the player.
     * @param player The person to receive the message.
     */
    public void send(Player player) {
        this.send(new RosePlayer(player));
    }

    public String getWarning() {
        return this.warning;
    }

}
