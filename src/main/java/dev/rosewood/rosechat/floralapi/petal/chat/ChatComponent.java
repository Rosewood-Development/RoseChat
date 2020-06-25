package dev.rosewood.rosechat.floralapi.petal.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

public class ChatComponent {

    private String message;
    private HoverEvent hoverEvent;
    private ClickEvent clickEvent;
    private String color = "#FFFFFF";

    public ChatComponent() {

    }

    public ChatComponent(String message) {
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    public ChatComponent(String message, HoverEvent hoverEvent) {
        this(message);
        this.hoverEvent = hoverEvent;
    }

    public ChatComponent(String message, ClickEvent clickEvent) {
        this(message);
        this.clickEvent = clickEvent;
    }

    public ChatComponent(String message, HoverEvent hoverEvent, ClickEvent clickEvent) {
        this(message, hoverEvent);
        this.clickEvent = clickEvent;
    }

    public ChatComponent setText(String value) {
        this.message = value;
        return this;
    }

    public ChatComponent setColor(String color) {
        this.color = color;
        return this;
    }

    public ChatComponent setHoverEvent(HoverEvent.Action action, String value) {
        BaseComponent[] nValue = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', value));
        this.hoverEvent = new HoverEvent(action, nValue);
        return this;
    }

    public ChatComponent setClickEvent(ClickEvent.Action action, String value) {
        this.clickEvent = new ClickEvent(action, ChatColor.translateAlternateColorCodes('&', value));
        return this;
    }

    public String getMessage() {
        return message;
    }

    public HoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public ClickEvent getClickEvent() {
        return clickEvent;
    }

    public String getColor() {
        return color;
    }
}
