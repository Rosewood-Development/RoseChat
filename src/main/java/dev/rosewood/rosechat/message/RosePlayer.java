package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.channel.ChannelChangeEvent;
import dev.rosewood.rosechat.api.event.player.PlayerNicknameEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.placeholder.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 * Class for managing players and console messages.
 */
@SuppressWarnings({"deprecation", "unused"})
public class RosePlayer {

    private final RoseChatAPI api;
    private List<String> ignoredPermissions;
    private boolean isDiscordProxy;
    private OfflinePlayer offlinePlayer;
    private String name;
    private String group;

    private RosePlayer() {
        this.api = RoseChatAPI.getInstance();
        this.ignoredPermissions = new ArrayList<>();
    }

    public RosePlayer(Player player) {
        this();

        this.offlinePlayer = player;
        this.name = player.getName();
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
            this.name = player.getName();
            this.group = this.api.getVault() == null ? "default" : this.api.getVault().getPrimaryGroup(player);
        } else {
            this.name = "Console";
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
            this.name = player.getName();
            this.group = this.api.getVault() == null ?
                    "default" : this.api.getVault().getPrimaryGroup(player);
        } else {
            this.offlinePlayer = offlinePlayer;
            this.name = offlinePlayer.getName();
            this.group = this.api.getVault() == null ?
                    "default" : this.api.getVault().getPrimaryGroup(null, offlinePlayer);
        }
    }

    /**
     * Creates a new RosePlayer.
     * @param name The name to use.
     * @param isDiscordUser Whether this RosePlayer is sending a message from Discord.
     */
    public RosePlayer(String name, boolean isDiscordUser) {
        this();

        this.name = name;
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

        this.name = name;
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
        this.name = name;
        this.group = group;
    }

    // Nickname Functions

    /**
     * @return The player's username, or display name if the player doesn't exist.
     */
    public String getRealName() {
        return this.offlinePlayer == null ? this.name : this.offlinePlayer.getName();
    }

    /**
     * @return The display name.
     */
    public String getDisplayName() {
        return this.isPlayer() ? this.asPlayer().getDisplayName() : this.name;
    }

    public void setDisplayName(String displayName) {
        if (this.isPlayer())
            this.asPlayer().setDisplayName(displayName);
        else
            this.name = displayName;
    }

    public void setDisplayName(MessageTokenizerResults message) {
        if (message == null) {
            this.setDisplayName((String) null);
            return;
        }

        if (this.isPlayer())
            message.setDisplayName(this.asPlayer());
        else
            this.name = message.build(TokenComposer.plain());
    }

    /**
     * @return A nickname or display name if the player is online. If not, then the player's username.
     */
    public String getName() {
        if (!this.isPlayer())
            return this.name;

        String nickname = this.getPlayerData().getNickname();
        return nickname == null ? this.name : nickname;
    }

    /**
     * @return The nickname of a player, will return null if the player has no nickname.
     */
    public String getNickname() {
        if (!this.isPlayer())
            return null;

        return this.getPlayerData().getNickname();
    }

    /**
     * @param callback A callback containing the player's saved nickname.
     */
    public void getName(Consumer<String> callback) {
        if (this.isConsole() || this.isPlayer()) {
            callback.accept(this.getNickname());
            return;
        }

        // If the player is offline, get the nickname from the database.
        this.api.getPlayerData(this.offlinePlayer.getUniqueId(), (data) -> {
            if (data == null) {
                callback.accept(this.name);
                return;
            }

            String nickname = data.getNickname();
            callback.accept(nickname == null ? this.name : data.getNickname());
        });
    }

    /**
     * @return Components containing the parsed nickname.
     */
    public MessageTokenizerResults getParsedNickname() {
        String nickname = this.getNickname();
        String name = nickname == null ? this.name : nickname;
        RoseMessage message = RoseMessage.forLocation(this, PermissionArea.NICKNAME);
        return message.parse(this, name);
    }

    /**
     * @param callback A callback containing the player's saved nickname, after being parsed.
     */
    public void getParsedNickname(Consumer<MessageTokenizerResults> callback) {
        if (this.isConsole() || this.isPlayer()) {
            callback.accept(this.getParsedNickname());
            return;
        }

        // If the player is offline, get the nickname from the database.
        this.api.getPlayerData(this.offlinePlayer.getUniqueId(), (data) -> {
            String nickname = this.getNickname();
            String name = nickname == null ? this.name : nickname;
            RoseMessage message = RoseMessage.forLocation(this, PermissionArea.NICKNAME);
            MessageTokenizerResults components = message.parse(this, name);
            callback.accept(components);
        });
    }

    /**
     * Updates the player's nickname.
     * @param nickname The unformatted nickname to use.
     */
    public boolean setNickname(String nickname) {
        if (!this.isPlayer()) {
            this.name = nickname;
            return true;
        }

        PlayerNicknameEvent event = new PlayerNicknameEvent(this.asPlayer(), nickname);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        nickname = event.getNickname();
        this.getPlayerData().setNickname(nickname);

        this.updateDisplayName();

        this.getPlayerData().save();

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

    public void updateDisplayName() {
        if (this.getPlayerData().getNickname() == null) {
            if (RoseChat.getInstance().getNicknameProvider() != null)
                RoseChat.getInstance().getNicknameProvider()
                        .setNickname(this.asPlayer(), null);

            if (Settings.UPDATE_PLAYER_LIST.get() && this.isPlayer())
                this.asPlayer().setPlayerListName(null);

            this.setDisplayName((String) null);

            this.getPlayerData().setStrippedDisplayName(this.getRealName());
            return;
        }

        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            MessageTokenizerResults parsedNickname = this.getParsedNickname();
            this.setDisplayName(parsedNickname);

            if (RoseChat.getInstance().getNicknameProvider() != null)
                RoseChat.getInstance().getNicknameProvider()
                        .setNickname(this.asPlayer(), this.getDisplayName());

            if (Settings.UPDATE_PLAYER_LIST.get() && this.isPlayer())
                this.asPlayer().setPlayerListName(name);

            this.getPlayerData().setStrippedDisplayName(parsedNickname.build(TokenComposer.plain()));
        });
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
            ChannelMessageOptions options = new ChannelMessageOptions.Builder()
                    .sender(this)
                    .message(message)
                    .build();
            channel.send(options);
            return;
        }

        this.getPlayerData().setActiveChannel(channel);

        AsyncPlayerChatEvent asyncPlayerChatEvent =
                new AsyncPlayerChatEvent(!Bukkit.isPrimaryThread(), this.asPlayer(), message, Collections.emptySet());
        Bukkit.getPluginManager().callEvent(asyncPlayerChatEvent);

        this.getPlayerData().setActiveChannel(null);
    }

    /**
     * Removes the player's mute if it has expired.
     */
    public void validateMuteExpiry() {
        if (this.getPlayerData().isMuteExpired())
            this.unmute();
    }

    /**
     * Removes the player's chat color if they no longer have permission.
     */
    public void validateChatColor() {
        if (!MessageUtils.canColor(this, this.getPlayerData().getColor(), PermissionArea.CHATCOLOR))
            this.getPlayerData().setColor("");
    }

    /**
     * Removes and re-adds the chat completions for the player.
     */
    public void validateChatCompletion() {
        if (!this.isPlayer())
            return;

        Player player = this.asPlayer();
        player.removeCustomChatCompletions(this.getPlayerData().getChatCompletions());

        List<String> completions = new ArrayList<>();

        for (Filter filter : api.getFilters()) {
            if (filter.useRegex() || filter.matches().isEmpty())
                continue;

            if (!filter.addToSuggestions())
                continue;

            for (String match : filter.matches()) {
                if (filter.hasPermission(this))
                    completions.add(match);
            }

            if (filter.prefix() != null && filter.hasPermission(this))
                completions.add(filter.prefix());
        }

        for (CustomPlaceholder placeholder : api.getPlaceholderManager().getPlaceholders().values()) {
            if (!this.hasPermission("rosechat.placeholder." + placeholder.getId()))
                continue;

            completions.add("{" + placeholder.getId() + "}");
        }

        player.addCustomChatCompletions(completions);
        this.getPlayerData().setChatCompletions(completions);
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
        else if (this.isConsole()) {
            this.logToConsole(message);
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

    /**
     * Sends a message to the RosePlayer.
     * @param message The message to send.
     */
    public void send(BaseComponent[] message) {
        if (this.isPlayer())
            this.asPlayer().spigot().sendMessage(message);
        else if (this.isConsole()) {
            this.logToConsole(TextComponent.toPlainText(message));
            Bukkit.getConsoleSender().spigot().sendMessage(message);
        }
    }

    /**
     * Sends a message to the RosePlayer.
     * @param message The message to send.
     */
    public void send(MessageTokenizerResults message) {
        if (this.isPlayer())
            message.sendMessage(this.asPlayer());
        else if (this.isConsole()) {
            this.logToConsole(message.build(TokenComposer.plain()));
            message.sendMessage(Bukkit.getConsoleSender());
        }
    }

    private void logToConsole(String message) {
        RoseChat plugin = RoseChat.getInstance();
        if (plugin.getConsoleLog() == null)
            return;

        SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss] ");
        String time = sdf.format(new Date());
        RoseChat.getInstance().getConsoleLog().addMessage(time + message);
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

        if (oldChannel != null && !this.getPlayerData().isCurrentChannelGroupChannel())
            oldChannel.onLeave(this);

        if (!isGroupChannel)
            channel.onJoin(this);

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
     * Finds the appropriate channel to put the player in.
     * This is mainly used in instances where a player is forcefully removed from a channel and
     * needs to join a new channel.
     * @return The {@link Channel} that the player should move to.
     */
    public Channel findChannel() {
        if (!this.isPlayer())
            return this.api.getDefaultChannel();

        Channel foundChannel = null;
        for (Channel channel : this.api.getChannels()) {
            if (channel.onWorldJoin(this, null, this.asPlayer().getWorld()))
                foundChannel = channel;
        }

        if (foundChannel == null)
            foundChannel = this.api.getDefaultChannel();

        return foundChannel;
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
        if (this.isConsole() || (this.isDiscordProxy && !Settings.REQUIRE_PERMISSIONS.get()))
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
               return !Settings.REQUIRE_PERMISSIONS.get() || this.offlinePlayer.isOp()
                       || this.api.getVault().playerHas(null, this.offlinePlayer, permission);
           }
       }

        // If the player is not available, check the group permissions as long as we have Vault
        if (this.group != null && this.api.getVault() != null)
            return !Settings.REQUIRE_PERMISSIONS.get() || this.api.getVault().groupHas((String) null, this.group, permission);

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
     * @return True if the RosePlayer is an offline player.
     */
    public boolean isOffline() {
        return this.offlinePlayer != null && this.offlinePlayer.getPlayer() == null;
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
        if (!this.isOffline()) {
            callback.accept(this.getPlayerData());
        } else {
            this.api.getPlayerData(this.offlinePlayer.getUniqueId(), callback);
        }
    }

    public void setDiscordProxy(boolean discordProxy) {
        this.isDiscordProxy = discordProxy;
    }

}
