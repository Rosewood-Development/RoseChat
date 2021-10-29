package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Class for managing 'fake' players and console messages.
 */
public class RoseSender {

    private RoseChat plugin;
    private Player player;
    private String displayName;
    private String group;

    /**
     * Creates a new RoseSender.
     * @param player The player to use.
     */
    public RoseSender(Player player) {
        this.plugin = RoseChat.getInstance();
        this.player = player;
        this.displayName = player.getDisplayName();
        this.group = this.plugin.getVault() == null ? "default" : this.plugin.getVault().getPrimaryGroup(player);
    }

    /**
     * Creates a new RoseSender.
     * @param sender The CommandSender to use.
     */
    public RoseSender(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            this.plugin = RoseChat.getInstance();
            this.player = player;
            this.displayName = player.getDisplayName();
            this.group = this.plugin.getVault() == null ? "default" : this.plugin.getVault().getPrimaryGroup(player);
        } else {
            this.displayName = "&cConsole";
            this.group = "default";
        }
    }

    /**
     * Creates a new RoseSender.
     * @param name The name to use.
     * @param group The group to use.
     */
    public RoseSender(String name, String group) {
        this.displayName = name;
        this.group = group;
    }

    /**
     * @param permission The permission to check for.
     * @return True if the RoseSender has the permission.
     */
    public boolean hasPermission(String permission) {
        return this.player == null || this.player.hasPermission(permission);
    }

    /**
     * @return True if the RoseSender is a player.
     */
    public boolean isPlayer() {
        return this.player != null;
    }

    /**
     * @return True if the RoseSender is a Console.
     */
    public boolean isConsole() {
        return this.player == null;
    }

    /**
     * Sends a message to the RoseSender.
     * @param message The message to send.
     * @return True if a message was sent.
     */
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

    /**
     * Sends a message to the RoseSender.
     * @param message The message to send.
     * @return True if a message was sent.
     */
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

    /**
     * @return A Player from the RoseSender.
     */
    public Player asPlayer() {
        if (this.isPlayer()) return this.player;
        else return null;
    }

    /**
     * @return The UUID of the player.
     */
    public UUID getUUID() {
        if (this.isPlayer()) return this.player.getUniqueId();
        else return null;
    }

    /**
     * @return The group of the RoseSender.
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Sets the RoseSender's group.
     * @param group The group to use.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return The display name.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @return If a player, their nickname. If not, returns the display name.
     */
    public String getNickname() {
        if (!this.isPlayer()) return this.getDisplayName();
        String nickname = RoseChatAPI.getInstance().getPlayerData(this.getUUID()).getNickname();
        return nickname == null ? this.getDisplayName() : nickname;
    }

    /**
     * @return If a player, their name. If not, returns the display name.
     */
    public String getName() {
        return this.isConsole() ? this.getDisplayName() : this.asPlayer().getName();
    }
}
