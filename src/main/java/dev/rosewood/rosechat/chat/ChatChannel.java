package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatChannel implements Group {

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
            if (data.getMuteTime() > 0 && data.getMuteTime() < System.currentTimeMillis()) {
                data.setMuteTime(0);
                data.save();
            }

            // Don't send the message if the player is muted.
            if (data.getMuteTime() != 0 && !sender.hasPermission("rosechat.mute.bypass")) {
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

    /**
     * Sends a message to the channel.
     * @param message The {@link MessageWrapper} to send.
     */
    private void sendGeneric(MessageWrapper message, String format, boolean sendToDiscord, boolean sendToBungee) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message to the channel spies.
        if (!this.visibleAnywhere) {
            for (UUID uuid : api.getDataManager().getChannelSpies()) {
                if (this.getMembers().contains(uuid) || uuid == message.getSender().getUUID()) continue;
                Player spy = Bukkit.getPlayer(uuid);
                if (spy != null)
                    spy.spigot().sendMessage(message.parse(Setting.CHANNEL_SPY_FORMAT.getString(), new RoseSender(spy)));
            }
        }

        // Send the message to discord.
        if (sendToDiscord && api.getDiscord() != null && this.discordChannel != null) {
            MessageUtils.sendDiscordMessage(message, this, this.discordChannel);
        }

        // Send the message to other servers.
        if (sendToBungee && api.isBungee()) {
            for (String server : this.servers)
                BungeeListener.sendChannelMessage(message.getSender(), server, this.getId(), message.getMessage());
        }

        // Send to everyone who can view it.
        if (this.isVisibleAnywhere()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = api.getPlayerData(player.getUniqueId());
                if (data != null && data.getIgnoringPlayers().contains(message.getSender().getUUID()) || !player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(message.parse(format, new RoseSender(player)));

                this.sendTagSound(message, player, data);
            }

            return;
        }

        // Send to players within the radius.
        if (this.getRadius() != -1 && message.getSender().isPlayer()) {
            Location senderLocation = message.getSender().asPlayer().getLocation();
            if (senderLocation.getWorld() == null) return;

            for (Player player : senderLocation.getWorld().getPlayers()) {
                PlayerData data = api.getPlayerData(player.getUniqueId());
                if (data != null && data.getIgnoringPlayers().contains(message.getSender().getUUID())) continue;

                if (player.getLocation().distance(senderLocation) < this.radius)
                    player.spigot().sendMessage(message.parse(format, new RoseSender(player)));

                this.sendTagSound(message, player, data);
            }

            return;
        }

        // Send to players in the same world.
        if (this.world != null) {
            World world = Bukkit.getWorld(this.world);
            if (world == null) return;

            for (Player player : world.getPlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;

                PlayerData data = api.getPlayerData(player.getUniqueId());
                if (data != null && data.getIgnoringPlayers().contains(message.getSender().getUUID())) continue;

                player.spigot().sendMessage(message.parse(format, new RoseSender(player)));
                this.sendTagSound(message, player, data);
            }

            return;
        }

        // If none of the other conditions were met, send to all the players in the channel.
        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;

                PlayerData data = api.getPlayerData(player.getUniqueId());
                if (data != null && data.getIgnoringPlayers().contains(message.getSender().getUUID())) continue;

                player.spigot().sendMessage(message.parse(format, new RoseSender(player)));
                this.sendTagSound(message, player, data);
            }
        }
    }

    @Override
    public void send(MessageWrapper message) {
        this.sendGeneric(message, this.getFormat(), true, true);
    }

    @Override
    public void sendJson(RoseSender sender, String rawMessage) {
        MessageWrapper message = new MessageWrapper(sender, MessageLocation.CHANNEL, this, rawMessage).filter().applyDefaultColor();
        this.sendGeneric(message, this.getFormat(), true, false);
    }

    @Override
    public void sendFromDiscord(String id, MessageWrapper message) {
        this.sendGeneric(message, Setting.DISCORD_TO_MINECRAFT_FORMAT.getString(), false, true);
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
            for (String server : this.servers) {
                BungeeListener.sendChannelMessage(new RoseSender("", ""), server, this.id, message);
            }
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

}
