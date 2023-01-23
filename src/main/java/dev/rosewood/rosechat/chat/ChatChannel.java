package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatChannel implements Group {

    private final RoseChatAPI api;
    private final String id;
    private boolean defaultChannel;
    private boolean muted;
    private String format;
    private String command;
    private int radius;
    private String world;
    private boolean autoJoin;
    private boolean visibleAnywhere;
    private boolean joinable;
    private boolean keepFormatOverBungee;
    private String discordChannel;
    private List<String> servers;
    private List<UUID> players;

    /**
     * Creates a new chat channel with the given ID.
     * @param id The ID of the channel.
     * @param format The format to use for this channel.
     * @param defaultChannel Whether this should be the channel players join the first time they join the server.
     */
    public ChatChannel(String id, String format, boolean defaultChannel) {
        this.api = RoseChatAPI.getInstance();
        this.id = id;
        this.format = format;
        this.defaultChannel = defaultChannel;
        this.joinable = true;
        this.players = new ArrayList<>();
        this.servers = new ArrayList<>();
        this.radius = -1;
    }

    /**
     * Creates a new chat channel with the given ID.
     * @param id The ID of the channel.
     * @param format The format to use for this channel.
     */
    public ChatChannel(String id, String format) {
        this(id, format, false);
    }

    /**
     * Can a message be sent, by the sender, in this channel?
     * @param sender The {@link RoseSender} who sent the message.
     * @param message The message that the sender is trying to send.
     * @return True if the player can send the message.
     */
    public boolean canSendMessage(RoseSender sender, String message) {
        // Don't send the message if the player doesn't have permission.
        if (!sender.hasPermission("rosechat.chat") || !sender.hasPermission("rosechat.channel." + this.id)) {
            sender.sendLocaleMessage("no-permission");
            return false;
        }

        PlayerData data = sender.getPlayerData();
        if (data != null) {
            // Check Mute Expiry
            if (data.isMuteExpired()) {
                data.unmute();
                data.save();
            }

            // Don't send the message if the player is muted.
            if (data.isMuted() && !sender.hasPermission("rosechat.mute.bypass")) {
                sender.sendLocaleMessage("command-mute-cannot-send");
                return false;
            }
        }

        // Check if the channel can be sent to.
        if (this.muted && !sender.hasPermission("rosechat.mute.bypass")) {
            sender.sendLocaleMessage("channel-muted");
            return false;
        }

        // Make sure the message isn't blank.
        if (MessageUtils.isMessageEmpty(message)) {
            sender.sendLocaleMessage("message-blank");
            return false;
        }

        return true;
    }

    private boolean canReceiveMessage(Player receiver, PlayerData data, UUID senderUUID) {
        return (data != null && !data.getIgnoringPlayers().contains(senderUUID) && receiver.hasPermission("rosechat.channel." + this.getId()));
    }

    private void sendToPlayer(Player receiver, MessageWrapper message, String format, String discordId, boolean parse) {
        PlayerData data = this.api.getPlayerData(receiver.getUniqueId());
        if (!this.canReceiveMessage(receiver, data, message.getSender().getUUID())) return;
        RoseSender roseSender = new RoseSender(receiver);
        if (parse) {
            receiver.spigot().sendMessage(discordId == null ?
                    message.parse(format, roseSender) : message.parseFromDiscord(discordId, format, roseSender));
        } else {
            // May edit messages with "%other_" in them, this is a temporary fix and will be adjusted later
            receiver.spigot().sendMessage(ComponentSerializer.parse(PlaceholderAPIHook.applyPlaceholders(receiver, message.getMessage().replace("%other_", "%"))));
            data.getMessageLog().addDeletableMessage(new DeletableMessage(message.getId(), message.getMessage(), false, discordId));
        }

        this.sendTagSound(message, receiver, data);
    }

    private void sendGenericVisible(MessageWrapper message, String format, String discordId, boolean parse) {
        List<UUID> channelSpies = this.api.getPlayerDataManager().getChannelSpies();
        List<Player> currentSpies = new ArrayList<>();

        if (this.getRadius() != -1 && message.getSender().isPlayer()) {
            // Send to all players within the radius.
            Location senderLocation = message.getSender().asPlayer().getLocation();
            if (senderLocation.getWorld() == null) return;

            for (Player player : senderLocation.getWorld().getPlayers()) {
                if (player.getUniqueId() == message.getSender().getUUID()) continue;
                if (player.getLocation().distance(senderLocation) < this.radius) {
                    this.sendToPlayer(player, message, format, discordId, parse);
                } else {
                    // Allow spies to see the message if out of radius.
                    if (channelSpies.contains(player.getUniqueId())) currentSpies.add(player);
                }
            }

            // Allow spies to see the message if not in the same world.
            for (UUID uuid : channelSpies) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;;
                if (!player.getWorld().getName().equals(senderLocation.getWorld().getName())) currentSpies.add(player);
            }
        } else if (this.world != null) {
            // Send to all players within the world.
            World world = Bukkit.getWorld(this.world);
            if (world == null) return;

            for (Player player : world.getPlayers()) {
                if (player.getUniqueId() == message.getSender().getUUID()) continue;
                this.sendToPlayer(player, message, format, discordId, parse);
            }

            // Allow spies to see the message if not in the same world.
            for (UUID uuid : channelSpies) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                if (!player.getWorld().getName().equals(world.getName()))
                    if (channelSpies.contains(player.getUniqueId())) currentSpies.add(player);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId() == message.getSender().getUUID()) continue;
                this.sendToPlayer(player, message, format, discordId, parse);
            }
        }

        for (Player spy : currentSpies) {
            if (spy == null) continue;
            if (message.getSender().getUUID() == spy.getUniqueId()) continue;
            spy.spigot().sendMessage(message.parse(Setting.CHANNEL_SPY_FORMAT.getString(), new RoseSender(spy)));
        }
    }

    private void sendGenericHidden(MessageWrapper message, String format, String discordId, boolean parse) {
        List<UUID> channelSpies = this.api.getPlayerDataManager().getChannelSpies();
        List<Player> currentSpies = new ArrayList<>();

        if (this.getRadius() != -1 && message.getSender().isPlayer()) {
            // Send to all members within the radius.
            Location senderLocation = message.getSender().asPlayer().getLocation();
            if (senderLocation.getWorld() == null) return;

            for (UUID uuid : this.getMembers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || player.getUniqueId() == message.getSender().getUUID()) continue;
                if (player.getWorld().getName().equals(senderLocation.getWorld().getName()) && player.getLocation().distance(senderLocation) < this.radius) {
                    this.sendToPlayer(player, message, format, discordId, parse);
                } else {
                    currentSpies.add(player);
                }
            }

            // Allow spies to see the message if not in the same world.
            for (UUID uuid : channelSpies) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;;
                if (!player.getWorld().getName().equals(senderLocation.getWorld().getName())) currentSpies.add(player);
            }
        } else if (this.world != null) {
            for (UUID uuid : this.getMembers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || player.getUniqueId() == message.getSender().getUUID()) continue;
                if (player.getWorld().getName().equals(this.world)) {
                    this.sendToPlayer(player, message, format, discordId, parse);
                } else {
                    currentSpies.add(player);
                }
            }
        } else {
            for (UUID uuid : this.getMembers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || player.getUniqueId() == message.getSender().getUUID()) continue;
                this.sendToPlayer(player, message, format, discordId, parse);
            }
        }

        for (UUID uuid : channelSpies) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null) continue;
            if (message.getSender().getUUID() == spy.getUniqueId()) continue;
            if (this.getMembers().contains(uuid) && !currentSpies.contains(spy)) continue;
            spy.spigot().sendMessage(message.parse(Setting.CHANNEL_SPY_FORMAT.getString(), new RoseSender(spy)));
        }
    }

    /**
     * Sends a message to the channel.
     * @param message The {@link MessageWrapper} to send.
     */
    private void sendGeneric(MessageWrapper message, String format, boolean sendToDiscord, boolean sendToBungee, String discordId, boolean parse) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message to the sender. Always happens.
        Player sender = message.getSender().isPlayer() ? message.getSender().asPlayer() : null;
        PlayerData senderData = sender != null ? api.getPlayerData(sender.getUniqueId()) : null;
        if (senderData != null) {
            sender.spigot().sendMessage(discordId == null ? message.parse(format, message.getSender()) : message.parseFromDiscord(discordId, format, message.getSender()));
        }

        // Send the message to discord. Always happens.
        if (sendToDiscord && api.getDiscord() != null && this.discordChannel != null) {
            if (parse && !this.keepFormatOverBungee) MessageUtils.sendDiscordMessage(message, this, this.discordChannel);
        }

        // Send the message to other servers. Always happens.
        if (sendToBungee && api.isBungee()) {
            for (String server : this.servers) {
                if (this.keepFormatOverBungee) {
                    api.getBungeeManager().sendChannelMessage(message.getSender(), server, this.getId(), message.getId(), message.parseToBungee(this.getFormat(), message.getSender()));
                } else {
                    api.getBungeeManager().sendChannelMessage(message.getSender(), server, this.getId(), message.getId(), message.getMessage());
                }
            }
        }

        // Settings should act different if visible anywhere is enabled or disabled, so we handle them differently.
        if (this.isVisibleAnywhere()) {
            this.sendGenericVisible(message, format, discordId, parse);
        } else {
            this.sendGenericHidden(message, format, discordId, parse);
        }
    }

    @Override
    public void send(MessageWrapper message) {
        this.sendGeneric(message, this.getFormat(), true, true, null, true);
    }

    @Override
    public void sendJson(RoseSender sender, UUID messageId, String rawMessage) {
        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            if (rawMessage.startsWith("{")) {
                MessageWrapper message = new MessageWrapper(sender, MessageLocation.CHANNEL, this, rawMessage);
                message.setId(messageId);
                this.sendGeneric(message, this.getFormat(), true, false, null, false);
            } else {
                MessageWrapper message = new MessageWrapper(sender, MessageLocation.CHANNEL, this, rawMessage).filter().applyDefaultColor();
                message.setId(messageId);
                this.sendGeneric(message, this.getFormat(), true, false, null, true);
            }
        });
    }

    @Override
    public void sendFromDiscord(String id, MessageWrapper message) {
        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            this.sendGeneric(message, Setting.DISCORD_TO_MINECRAFT_FORMAT.getString(), false, true, id, true);
        });
    }

    /**
     * Sends the tag sound to the specific player.
     * @param message The {@link MessageWrapper} containing the tagged players.
     * @param player The player that was tagged.
     * @param data The data for the tagged player.
     */
    private void sendTagSound(MessageWrapper message, Player player, PlayerData data) {
        if (message.getTaggedPlayers().contains(player.getUniqueId())) {
            if (message.getTagSound() != null && (data != null && data.hasTagSounds()))
                player.playSound(player.getLocation(), message.getTagSound(), 1.0f, 1.0f);
        }
    }

    @Override
    public List<UUID> getMembers() {
        return this.players;
    }

    @Override
    public String getLocationPermission() {
        return this.getId();
    }

    /**
     * Clears the channel's chat.
     * @param message The message to clear with.
     */
    public void clear(String message) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message to other servers.
        if (api.isBungee()) {
            for (String server : this.servers)
                api.getBungeeManager().sendChannelMessage(new RoseSender("", ""), server, this.getId(), null, message);
        }

        // Send to everyone who can view it.
        if (this.isVisibleAnywhere()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.sendMessage(message);
            }

            return;
        }

        // Send to players in the same world.
        if (this.world != null) {
            World world = Bukkit.getWorld(this.world);

            if (world == null) return;
            for (Player player : world.getPlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.sendMessage(message);
            }

            return;
        }

        // Send to players in the channel.
        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.sendMessage(message);
            }
        }
    }

    /**
     * Adds a player to the channel.
     * @param player The player to add.
     */
    public void add(Player player) {
        add(player.getUniqueId());
    }

    /**
     * Removes a player from the channel.
     * @param player The player to remove.
     */
    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    /**
     * Checks if the channel contains a player.
     * @param player The player to check for.
     * @return True if the channel contains the given player.
     */
    public boolean contains(Player player) {
        return contains(player.getUniqueId());
    }

    /**
     * Adds a UUID to the channel.
     * @param uuid The UUID to add.
     */
    public void add(UUID uuid) {
        this.players.add(uuid);
    }

    /**
     * Removes a UUID from the channel.
     * @param uuid The UUID to remove.
     */
    public void remove(UUID uuid) {
        this.players.remove(uuid);
    }

    /**
     * Checks if the channel contains a UUID.
     * @param uuid The UUID to check for.
     * @return True if the channel contains the given UUID.
     */
    public boolean contains(UUID uuid) {
        return this.players.contains(uuid);
    }

    /**
     * @return The ID of the channel.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return True if the channel is the default channel.
     */
    public boolean isDefaultChannel() {
        return this.defaultChannel;
    }

    /**
     * Sets the channel to being the default channel.
     * @param defaultChannel Whether the channel should be the default channel.
     */
    public void setDefaultChannel(boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    /**
     * @return The channel format.
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * @param format The format to use.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return The radius that messages can be received within.
     */
    public int getRadius() {
        return this.radius;
    }

    /**
     * Sets the channel radius.
     * @param radius The radius to use.
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * @return The world that messages from this channel can be sent and received in.
     */
    public String getWorld() {
        return this.world;
    }

    /**
     * Sets the world.
     * @param world The world to use.
     */
    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * @return True if players will automatically join the channel when entering the world.
     */
    public boolean isAutoJoin() {
        return this.autoJoin;
    }

    /**
     * @param autoJoin Whether players will automatically join the channel when entering the world.
     */
    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    /**
     * Sets the players in the channel.
     * @param players The players to be placed in the channel.
     */
    public void setPlayers(List<UUID> players) {
        this.players = players;
    }

    /**
     * @return The bungee servers the messages will be sent to.
     */
    public List<String> getServers() {
        return this.servers;
    }

    /**
     * Sets the bungee servers the messages will be sent to.
     * @param servers The servers to use.
     */
    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    /**
     * @return Whether this channel is visible anywhere.
     */
    public boolean isVisibleAnywhere() {
        return this.visibleAnywhere;
    }

    /**
     * Sets this channel as visible, meaning that messages will always be received even if a player is in another channel.
     * @param visibleAnywhere Whether the channel should be visible anywhere.
     */
    public void setVisibleAnywhere(boolean visibleAnywhere) {
        this.visibleAnywhere = visibleAnywhere;
    }

    /**
     * @return The command that can be used to access the channel.
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * Sets the command that can be used to access the channel.
     * @param command The command to be used.
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return Whether the channel is muted.
     */
    public boolean isMuted() {
        return this.muted;
    }

    /**
     * @param muted Whether the channel is muted.
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /**
     * Sets the DiscordSRV channel that the channel is connected to.
     * @param discordChannel The DiscordSRV channel to use.
     */
    public void setDiscordChannel(String discordChannel) {
        this.discordChannel = discordChannel;
    }

    /**
     * @return The DiscordSRV channel that the channel is connected to.
     */
    public String getDiscordChannel() {
        return this.discordChannel;
    }

    /**
     * @return Whether the channel can be joined.
     */
    public boolean isJoinable() {
        return this.joinable;
    }

    /**
     * @param joinable Whether the channel can be joined.
     */
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    /**
     * @return Whether the channel keeps the format when sending over BungeeCord.
     */
    public boolean shouldKeepFormatOverBungee() {
        return this.keepFormatOverBungee;
    }

    /**
     * @param keepFormatOverBungee Whether the channel keeps the format when sending over BungeeCord.
     */
    public void setShouldKeepFormatOverBungee(boolean keepFormatOverBungee) {
        this.keepFormatOverBungee = keepFormatOverBungee;
    }

}
