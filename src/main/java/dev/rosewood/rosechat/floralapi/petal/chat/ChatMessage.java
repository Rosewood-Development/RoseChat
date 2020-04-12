package dev.rosewood.rosechat.floralapi.petal.chat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Utility class for creating component based chat messages, with hover and click events.
 */
public class ChatMessage {

    /**
     * The message.
     */
    private String message;

    /**
     * The hover event.
     */
    private HoverEvent hoverEvent;

    /**
     * The click event.
     */
    private ClickEvent clickEvent;

    /**
     * Creates a new chat message with the given message.
     * @param message The base text.
     */
    public ChatMessage(String message) {
        this.message = message;
    }

    /**
     * Adds a hover event to the message.
     * @param action The hover action to use.
     * @param message The message shown. May not be a message.
     * @return An instance of this class.
     */
    public ChatMessage withHoverEvent(HoverEvent.Action action, String message) {
        this.hoverEvent = new HoverEvent(action, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        return this;
    }

    /**
     * Adds a click event to the message.
     * @param action The click action to use.
     * @param message The message shown. May not be a message.
     * @return An instance of this class.
     */
    public ChatMessage withClickEvent(ClickEvent.Action action, String message) {
        this.clickEvent = new ClickEvent(action, message);
        return this;
    }

    /**
     * Sends the message to a player.
     * @param player The player to send the message to.
     */
    public void send(Player player) {
        BaseComponent[] component = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
        for (int i = 0; i < component.length; i++) {
            component[i].setHoverEvent(hoverEvent);
            component[i].setClickEvent(clickEvent);
        }

        player.spigot().sendMessage(component);
    }

    /**
     * Sends an actionbar message to a player.
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }
}
