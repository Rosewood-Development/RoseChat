package dev.rosewood.rosechat.floralapi.petal.chat;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ChatMessage {

    private List<ChatComponent> components;

    public ChatMessage() {
        components = new ArrayList<>();
    }

    public ChatMessage addComponent(ChatComponent component) {
        components.add(component);
        return this;
    }

    /**
     * Sends the message to a player.
     * @param player The player to send the message to.
     */
    public void send(Player player) {
        ComponentBuilder builder = new ComponentBuilder();

        for (ChatComponent component : components) {
            BaseComponent[] textComponents = TextComponent.fromLegacyText(component.getMessage());
            if (component.getHoverEvent() != null || component.getClickEvent() != null) {
                TextComponent text = new TextComponent(textComponents);
                text.setHoverEvent(component.getHoverEvent());
                text.setClickEvent(component.getClickEvent());
                builder.append(text, FormatRetention.FORMATTING);
            } else {
                builder.append(textComponents, FormatRetention.FORMATTING);
            }
        }

        player.spigot().sendMessage(builder.create());
    }

    public void send(ConsoleCommandSender console) {
        StringBuilder sb = new StringBuilder();
        for (ChatComponent component : components)
            sb.append(component.getMessage());
        console.sendMessage(sb.toString());
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

    public List<ChatComponent> getComponents() {
        return components;
    }
}
