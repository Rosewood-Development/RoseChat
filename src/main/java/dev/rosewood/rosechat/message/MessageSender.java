package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

// Class for managing 'fake' players and console messages.
public class MessageSender {

    private RoseChat plugin;
    private Player player;
    private String name;
    private String group;

    public MessageSender(Player player) {
        this.plugin = RoseChat.getInstance();
        this.player = player;
        this.name = player.getName();
        this.group = this.plugin.getVault() == null ? "default" : this.plugin.getVault().getPrimaryGroup(player);
    }

    public MessageSender(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public boolean hasPermission(String permission) {
        return this.player == null || this.player.hasPermission(permission);
    }

    public boolean isPlayer() {
        return this.player != null;
    }

    public boolean isConsole() {
        return this.name.equalsIgnoreCase("console");
    }

    public boolean send(String message) {
        if (this.isPlayer()) {
            this.player.sendMessage(message);
        } else if (this.isConsole()) {
            Bukkit.getConsoleSender().sendMessage(message);
        } else {
            return false;
        }

        return true;
    }

    public void send(BaseComponent[] message) {

    }

    public Player asPlayer() {
        if (isPlayer()) return player;
        else return null;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
