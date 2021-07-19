package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Class for managing 'fake' players and console messages.
 */
public class MessageSender {

    private RoseChat plugin;
    private Player player;
    private String name;
    private String group;

    public MessageSender(Player player) {
        this.plugin = RoseChat.getInstance();
        this.player = player;
        this.name = player.getDisplayName();
        this.group = this.plugin.getVault() == null ? "default" : this.plugin.getVault().getPrimaryGroup(player);
    }

    public MessageSender(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            this.plugin = RoseChat.getInstance();
            this.player = player;
            this.name = player.getDisplayName();
            this.group = this.plugin.getVault() == null ? "default" : this.plugin.getVault().getPrimaryGroup(player);
        } else {
            this.name = "&cConsole";
            this.group = "default";
        }
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
        return this.player == null;
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

    public boolean send(BaseComponent[] message) {
        if (this.isPlayer()) {
            this.player.spigot().sendMessage(message);
        } else if (this.isConsole()) {
            Bukkit.getConsoleSender().spigot().sendMessage(message);
        } else {
            return false;
        }

        return true;
    }

    public Player asPlayer() {
        if (this.isPlayer()) return this.player;
        else return null;
    }

    public UUID getUUID() {
        if (this.isPlayer()) return this.player.getUniqueId();
        else return null;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
