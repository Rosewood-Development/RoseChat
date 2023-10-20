package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.channel.ChannelChangeEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for managing players and console messages.
 */
public class RosePlayer {

    private final RoseChatAPI api;
    private String displayName;
    private String group;
    private OfflinePlayer player;
    private List<String> ignoredPermissions;
    private boolean isDiscordUser;

    private RosePlayer() {
        this.api = RoseChatAPI.getInstance();
        this.ignoredPermissions = new ArrayList<>();
    }

    /**
     * Creates a new RosePlayer.
     * @param player The player to use.
     */
    public RosePlayer(Player player) {
        this();
        this.player = player;
        this.displayName = player.getDisplayName();
    }

    /**
     * Creates a new RosePlayer.
     * @param offlinePlayer The player to use.
     */
    public RosePlayer(OfflinePlayer offlinePlayer) {
        this();
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            this.player = player;
            this.displayName = player.getDisplayName();
        } else {
            this.player = offlinePlayer;
        }
    }

    /**
     * Creates a new RosePlayer.
     * @param sender The CommandSender to use.
     */
    public RosePlayer(CommandSender sender) {
        this();
        if (sender instanceof Player player) {
            this.player = player;
            this.displayName = player.getDisplayName();
        } else {
            this.displayName = "&cConsole";
            this.group = "default";
        }
    }

    /**
     * Creates a new RosePlayer.
     * @param name The name to use.
     * @param isDiscordUser Whether this RosePlayer is sending a message from Discord.
     */
    public RosePlayer(String name, boolean isDiscordUser) {
        this();
        this.displayName = name;
        this.group = "default";
        this.isDiscordUser = isDiscordUser;
    }

    /**
     * Creates a new RosePlayer.
     * @param name The name to use.
     * @param group The group to use.
     */
    public RosePlayer(String name, String group) {
        this();
        this.displayName = name;
        this.group = group;
    }

    /**
     * Creates a new RosePlayer.
     * @param uuid The player UUID to use.
     * @param name The name to use.
     * @param group The group to use.
     */
    public RosePlayer(UUID uuid, String name, String group) {
        this();
        this.player = Bukkit.getOfflinePlayer(uuid);
        this.displayName = name;
        this.group = group;
    }

    /**
     * @param permission The permission to check for.
     * @return True if the RosePlayer has the permission.
     */
    public boolean hasPermission(String permission) {
        if (this.isConsole())
            return true; // Console has all permissions

        // If the permission is ignored, return true
        if (this.ignoredPermissions.contains(permission.toLowerCase().substring("rosechat.".length())))
            return true;

        // Is the player online?
       if (this.player != null) {
           Player onlinePlayer = this.player.getPlayer();
           if (onlinePlayer != null)
               return onlinePlayer.hasPermission(permission);

           // Otherwise, check their offline permissions if Vault is available
           if (this.api.getVault() != null) {
               return !ConfigurationManager.Setting.REQUIRE_PERMISSIONS.getBoolean() || this.player.isOp()
                       || this.api.getVault().playerHas(null, this.player, permission);
           }
       }

        // If the player is not available, check the group permissions as long as we have Vault
        if (this.group != null && this.api.getVault() != null)
            return !ConfigurationManager.Setting.REQUIRE_PERMISSIONS.getBoolean() || this.api.getVault().groupHas((String) null, this.group, permission);

        // If none of the above worked, just allow it
        return true;
    }

    /**
     * @return A list of RoseChat permissions that this RosePlayer has.
     */
    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<>();
        Player player = this.asPlayer();
        if (player != null)
            for (PermissionAttachmentInfo permission : player.getEffectivePermissions())
                if (permission.getPermission().startsWith("rosechat."))
                    permissions.add(permission.getPermission().substring("rosechat.".length()));

        return permissions;
    }

    public void setIgnoredPermissions(List<String> permissions) {
        this.ignoredPermissions = permissions;
    }

    public List<String> getIgnoredPermissions() {
        return this.ignoredPermissions;
    }

    /**
     * @return True if the RosePlayer is a player.
     */
    public boolean isPlayer() {
        return this.player != null && this.player.isOnline();
    }

    /**
     * @return True if the RosePlayer is a Console.
     */
    public boolean isConsole() {
        return this.player == null && !this.isDiscordUser;
    }

    /**
     * Sends a localized message to the RosePlayer.
     * @param key The key of the message to send.
     */
    public void sendLocaleMessage(String key) {
        this.api.getLocaleManager().sendComponentMessage(this, key);
    }

    /**
     * Sends a message to the RosePlayer.
     * @param message The message to send.
     * @return True if a message was sent.
     */
    public boolean send(String message) {
        if (this.isPlayer()) {
            this.asPlayer().sendMessage(message);
        } else if (this.isConsole()) {
            Bukkit.getConsoleSender().sendMessage(message);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Sends a message to the RosePlayer.
     * @param message The message to send.
     * @return True if a message was sent.
     */
    public boolean send(BaseComponent[] message) {
        if (this.isPlayer()) {
            this.asPlayer().spigot().sendMessage(message);
        } else if (this.isConsole()) {
            Bukkit.getConsoleSender().spigot().sendMessage(message);
        } else {
            return false;
        }

        return true;
    }

    /**
     * @return A Player from the RosePlayer.
     */
    public Player asPlayer() {
        return this.player != null ? this.player.getPlayer() : null;
    }

    /**
     * @return The UUID of the player.
     */
    public UUID getUUID() {
        return this.player != null ? this.player.getUniqueId() : null;
    }

    /**
     * @return The group of the RosePlayer.
     */
    public String getGroup() {
        if (this.group == null) {
            if (this.player != null && this.player.isOnline()) {
                Player onlinePlayer = this.player.getPlayer();
                return this.group = this.api.getVault() == null ? "default" : this.api.getVault().getPrimaryGroup(onlinePlayer);
            } else {
                return "default";
            }
        } else {
            return this.group;
        }
    }

    /**
     * Sets the RosePlayer's group.
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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return If a player, their nickname. If not, returns the display name.
     */
    public String getNickname() {
        if (!this.isPlayer()) return this.getDisplayName();
        if (this.api.getPlayerData(this.getUUID()) == null) return this.getDisplayName();
        String nickname = this.api.getPlayerData(this.getUUID()).getNickname();
        return nickname == null ? this.getDisplayName() : nickname;
    }

    /**
     * @return If a player, their name. If not, returns the display name.
     */
    public String getName() {
        return !this.isPlayer() ? this.getDisplayName() : this.asPlayer().getName();
    }

    /**
     * @return The {@link PlayerData} associated with this RosePlayer.
     */
    public PlayerData getPlayerData() {
        return this.player != null ? this.api.getPlayerData(this.player.getUniqueId()) : null;
    }

    public boolean changeChannel(Channel oldChannel, Channel channel) {
        if (!this.isPlayer()) return false;

        ChannelChangeEvent channelChangeEvent = new ChannelChangeEvent(oldChannel, channel, this.asPlayer());
        Bukkit.getPluginManager().callEvent(channelChangeEvent);
        if (channelChangeEvent.isCancelled()) return false;

        if (oldChannel != null) oldChannel.onLeave(this.asPlayer());
        channel.onJoin(this.asPlayer());
        this.getPlayerData().setCurrentChannel(channel);
        this.getPlayerData().save();
        return true;
    }

}
