package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.channel.ChannelChangeEvent;
import dev.rosewood.rosechat.api.event.player.PlayerNicknameEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.hook.nickname.NicknameProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class for managing players and console messages.
 */
public class RosePlayer {

    private final RoseChatAPI api;
    private List<String> ignoredPermissions;
    private boolean isDiscordProxy;
    private OfflinePlayer offlinePlayer;
    private String displayName;
    private String group;

    private RosePlayer() {
        this.api = RoseChatAPI.getInstance();
        this.ignoredPermissions = new ArrayList<>();
    }

    public RosePlayer(Player player) {
        this();

        this.offlinePlayer = player;
        this.displayName = player.getName();
        this.group = this.api.getVault() == null ? "default": this.api.getVault().getPrimaryGroup(player);
    }

    /**
     * Creates a new RosePlayer.
     * @param commandSender The CommandSender to use.
     */
    public RosePlayer(CommandSender commandSender) {
        this();

        if (commandSender instanceof Player player) {
            this.offlinePlayer = player;
            this.displayName = player.getName();
            this.group = this.api.getVault() == null ? "default" : this.api.getVault().getPrimaryGroup(player);
        } else {
            this.displayName = "Console";
            this.group = "default";
        }
    }

    /**
     * Creates a new RosePlayer.
     * @param offlinePlayer The player to use.
     */
    public RosePlayer(OfflinePlayer offlinePlayer) {
        this();

        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            this.offlinePlayer = player;
            this.displayName = player.getName();
            this.group = this.api.getVault() == null ? "default" : this.api.getVault().getPrimaryGroup(player);
        } else {
            this.offlinePlayer = offlinePlayer;
            this.displayName = offlinePlayer.getName();
            this.group = this.api.getVault() == null ? "default" : this.api.getVault().getPrimaryGroup(null, offlinePlayer);
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
        this.isDiscordProxy = isDiscordUser;
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
     * @param uuid An offline player's UUID.
     * @param name The name to use.
     * @param group The group to use.
     */
    public RosePlayer(UUID uuid, String name, String group) {
        this();

        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.displayName = name;
        this.group = group;
    }

    // Nickname Functions

    /**
     * @return The player's username, or display name if the player doesn't exist.
     */
    public String getRealName() {
        return this.offlinePlayer == null ? this.displayName : this.offlinePlayer.getName();
    }

    /**
     * @return The display name.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;

        if (this.isPlayer())
            this.asPlayer().setDisplayName(displayName);
    }

    /**
     * @return A nickname or display name if the player is online. If not, then the player's username.
     */
    public String getName() {
        if (this.offlinePlayer == null || this.offlinePlayer.getPlayer() == null)
            return this.displayName;

        String nickname = this.getPlayerData().getNickname();
        return nickname == null ? this.displayName : nickname;
    }

    /**
     * @return The nickname of a player, will return null if the player has no nickname.
     */
    public String getNickname() {
        if (this.offlinePlayer == null || this.offlinePlayer.getPlayer() == null)
            return null;

        return this.getPlayerData().getNickname();
    }

    /**
     * @param callback A callback containing the player's saved nickname.
     */
    public void getName(Consumer<String> callback) {
        if (this.offlinePlayer == null || this.offlinePlayer.getPlayer() != null) {
            callback.accept(this.getName());
            return;
        }

        // If the player is offline, get the nickname from the database.
        this.api.getPlayerData(this.offlinePlayer.getUniqueId(), (data) -> {
            if (data == null) {
                callback.accept(this.displayName);
                return;
            }

            String nickname = data.getNickname();

            callback.accept(nickname == null ? this.displayName : data.getNickname());
        });
    }

    /**
     * @return Components containing the parsed nickname.
     */
    public BaseComponent[] getParsedNickname() {
        if (this.offlinePlayer == null || this.offlinePlayer.getPlayer() == null)
            return TextComponent.fromLegacyText(this.displayName);

        RoseMessage nicknameMessage = RoseMessage.forLocation(this, PermissionArea.NICKNAME);
        MessageTokenizerResults<BaseComponent[]> components = nicknameMessage.parse(this, this.getName());

        return components.content();
    }

    /**
     * @param callback A callback containing the player's saved nickname, after being parsed.
     */
    public void getParsedNickname(Consumer<BaseComponent[]> callback) {
        if (this.offlinePlayer == null || this.offlinePlayer.getPlayer() != null) {
            callback.accept(this.getParsedNickname());
            return;
        }

        // If the player is offline, get the nickname from the database.
        this.api.getPlayerData(this.offlinePlayer.getUniqueId(), (data) -> {
            if (data == null) {
                callback.accept(TextComponent.fromLegacyText(this.displayName));
                return;
            }

            String nickname = data.getNickname();
            if (nickname == null) {
                callback.accept(TextComponent.fromLegacyText(this.displayName));
                return;
            }

            RoseMessage nicknameMessage = RoseMessage.forLocation(this, PermissionArea.NICKNAME);
            MessageTokenizerResults<BaseComponent[]> components = nicknameMessage.parse(this, this.getName());
            callback.accept(components.content());
        });
    }

    /**
     * Updates the player's nickname.
     * @param nickname The unformatted nickname to use.
     */
    public boolean setNickname(String nickname) {
        if (!this.isPlayer()) {
            this.displayName = nickname;
            return true;
        }

        PlayerNicknameEvent event = new PlayerNicknameEvent(this.asPlayer(), nickname);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        this.getPlayerData().setNickname(nickname);
        this.getPlayerData().save();

        // Parse the nickname before setting the display name.
        if (nickname != null)
            nickname = TextComponent.toLegacyText(this.getParsedNickname());

        // Update the nickname with the provider to avoid any conflicts.
        NicknameProvider nicknameProvider = RoseChat.getInstance().getNicknameProvider();
        if (nicknameProvider != null)
            nicknameProvider.setNickname(this.asPlayer(), nickname);

        this.offlinePlayer.getPlayer().setDisplayName(nickname);

        return true;
    }

    /**
     * Removes a nickname.
     */
    public boolean removeNickname() {
        return this.setNickname(null);
    }

    /**
     * Updates the player's nickname color.
     * @param color The color to use.
     */
    public boolean setNicknameColor(String color) {
        String nickname = ChatColor.stripColor(HexUtils.colorify(this.getName()));
        nickname = color + nickname;

        return this.setNickname(nickname);
    }

    /**
     * Removes the nickname color.
     */
    public boolean removeNicknameColor() {
        PlayerData data = this.getPlayerData();

        if (data.getNickname() == null)
            return true;

        String currentNickname = data.getNickname();
        String colorizedNickname = HexUtils.colorify(currentNickname);
        String strippedNickname = ChatColor.stripColor(colorizedNickname);

        // Remove the player's nickname if they only have a colour.
        if (strippedNickname.equalsIgnoreCase(this.getRealName())) {
            this.setNickname(null);
            return true;
        }

        return this.setNickname(strippedNickname);
    }

    // Message Functions

    /**
     * Sends a message to the channel that the player is in.
     * @param message The message to send.
     */
    public void chat(String message) {
        if (this.isConsole())
            return;

        AsyncPlayerChatEvent asyncPlayerChatEvent =
                new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), this.asPlayer(), message, Collections.emptySet());
        Bukkit.getPluginManager().callEvent(asyncPlayerChatEvent);
    }

    /**
     * Sends a message to the specified channel without setting the player's current channel.
     * @param channel The channel to send to.
     * @param message The message to send.
     */
    public void quickChat(Channel channel, String message) {
        if (!this.isPlayer()) {
            channel.send(this, message);
            return;
        }

        this.getPlayerData().setActiveChannel(channel);

        AsyncPlayerChatEvent asyncPlayerChatEvent =
                new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), this.asPlayer(), message, Collections.emptySet());
        Bukkit.getPluginManager().callEvent(asyncPlayerChatEvent);

        this.getPlayerData().setActiveChannel(null);
    }

    /**
     * Prevents this player from chatting for the specified amount of time.
     * @param time How long the player should be muted for, in seconds.
     */
    public void mute(int time) {
        this.getPlayerData().mute((time * 1000L) + System.currentTimeMillis());
        this.getPlayerData().save();
    }

    /**
     * Prevents this player from chatting for an indefinite amount of time.
     */
    public void mute() {
        this.getPlayerData().mute(-1);
        this.getPlayerData().save();
    }

    /**
     * Unmutes this player.
     */
    public void unmute() {
        this.getPlayerData().unmute();
        this.getPlayerData().save();
    }

    public boolean isMuted() {
        return this.getPlayerData().isMuted();
    }

    /**
     * Sends a message to the RosePlayer.
     * @param message The message to send.
     */
    public void send(String message) {
        if (this.isPlayer())
            this.asPlayer().sendMessage(message);
        else if (this.isConsole())
            Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Sends a message to the RosePlayer.
     * @param message The message to send.
     */
    public void send(BaseComponent[] message) {
        if (this.isPlayer())
            this.asPlayer().spigot().sendMessage(message);
        else if (this.isConsole())
            Bukkit.getConsoleSender().spigot().sendMessage(message);
    }

    /**
     * Sends a localized message to the RosePlayer.
     * @param key The key of the message to send.
     */
    public void sendLocaleMessage(String key) {
        this.api.getLocaleManager().sendComponentMessage(this, key);
    }

    /**
     * Sends a localized message to the RosePlayer.
     * @param key The key of the message to send.
     * @param placeholders A set of {@link StringPlaceholders} to use.
     */
    public void sendLocaleMessage(String key, StringPlaceholders placeholders) {
        this.api.getLocaleManager().sendComponentMessage(this, key, placeholders);
    }

    // Channel Functions

    /**
     * @return The channel that the player is currently focused in.
     */
    public Channel getChannel() {
        return this.getPlayerData().getCurrentChannel();
    }

    /**
     * Moves this player from one channel to another.
     * @param channel The channel to move to.
     * @param isGroupChannel Whether the target channel is a {@link GroupChannel}.
     * @return True if the move was successful.
     */
    public boolean switchChannel(Channel channel, boolean isGroupChannel) {
        if (!this.isPlayer())
            return false;

        Channel oldChannel = this.getChannel();

        ChannelChangeEvent channelChangeEvent = new ChannelChangeEvent(oldChannel, channel, this.asPlayer());
        Bukkit.getPluginManager().callEvent(channelChangeEvent);
        if (channelChangeEvent.isCancelled())
            return false;

        if (oldChannel != null)
            oldChannel.onLeave(this.asPlayer());

        channel.onJoin(this.asPlayer());

        this.getPlayerData().setActiveChannel(channel);
        this.getPlayerData().setCurrentChannel(channel);
        this.getPlayerData().setIsInGroupChannel(isGroupChannel);
        this.getPlayerData().save();

        return true;
    }

    /**
     * Moves this player from one channel to another.
     * @param channel The channel to move to.
     * @return True if the move was successful.
     */
    public boolean switchChannel(Channel channel) {
        return this.switchChannel(channel, false);
    }

    /**
     * @return The group that this player owns, or null if they do not own one.
     */
    public GroupChannel getOwnedGroupChannel() {
        if (this.isConsole())
            return null;

        return this.api.getGroupChatByOwner(this.getUUID());
    }

    /**
     * Invites a player to a group channel.
     * @param group The {@link GroupChannel} inviting this player.
     */
    public void invite(GroupChannel group) {
        this.getPlayerData().inviteToGroup(group);
    }

    // Permission Functions

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
       if (this.offlinePlayer != null) {
           Player onlinePlayer = this.offlinePlayer.getPlayer();
           if (onlinePlayer != null)
               return onlinePlayer.hasPermission(permission);

           // Otherwise, check their offline permissions if Vault is available
           if (this.api.getVault() != null) {
               return !Setting.REQUIRE_PERMISSIONS.getBoolean() || this.offlinePlayer.isOp()
                       || this.api.getVault().playerHas(null, this.offlinePlayer, permission);
           }
       }

        // If the player is not available, check the group permissions as long as we have Vault
        if (this.group != null && this.api.getVault() != null)
            return !Setting.REQUIRE_PERMISSIONS.getBoolean() || this.api.getVault().groupHas((String) null, this.group, permission);

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

    /**
     * @param permissions A list of permissions to use.
     */
    public void setIgnoredPermissions(List<String> permissions) {
        this.ignoredPermissions = permissions;
    }

    /**
     * @return The permissions that this player should ignore.
     */
    public List<String> getIgnoredPermissions() {
        return this.ignoredPermissions;
    }

    // Misc Functions

    /**
     * @return True if the RosePlayer is a player.
     */
    public boolean isPlayer() {
        return this.offlinePlayer != null && this.offlinePlayer.getPlayer() != null;
    }

    /**
     * @return True if the RosePlayer is a Console.
     */
    public boolean isConsole() {
        return this.offlinePlayer == null && !this.isDiscordProxy;
    }

    /**
     * @return A player from the RosePlayer.
     */
    public Player asPlayer() {
        return this.isPlayer() ? this.offlinePlayer.getPlayer() : null;
    }

    /**
     * @return The UUID of the player.
     */
    public UUID getUUID() {
        return this.offlinePlayer != null ? this.offlinePlayer.getUniqueId() : null;
    }

    /**
     * @return The group of the RosePlayer.
     */
    public String getPermissionGroup() {
        if (this.group == null) {
            if (this.isPlayer()) {
                Player onlinePlayer = this.offlinePlayer.getPlayer();
                return this.group = (this.api.getVault() == null ?
                        "default" : this.api.getVault().getPrimaryGroup(onlinePlayer));
            } else {
                return "default";
            }
        }

        return this.group;
    }

    /**
     * Sets the RosePlayer's group.
     * @param group The group to use.
     */
    public void setPermissionGroup(String group) {
        this.group = group;
    }

    /**
     * @return The {@link PlayerData} associated with this RosePlayer.
     */
    public PlayerData getPlayerData() {
        return this.isPlayer() ? this.api.getPlayerData(this.offlinePlayer.getUniqueId()) : null;
    }

    /**
     * @param callback A callback containing the player's saved data.
     */
    public void getPlayerData(Consumer<PlayerData> callback) {
        if (!this.isPlayer())
            return;

        if (this.isPlayer()) {
            callback.accept(this.getPlayerData());
            return;
        }

        this.api.getPlayerData(this.offlinePlayer.getUniqueId(), callback);
    }

}
