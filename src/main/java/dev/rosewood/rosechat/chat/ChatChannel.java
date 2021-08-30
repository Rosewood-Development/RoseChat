package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.message.MessageWrapper;
import github.scarsz.discordsrv.dependencies.jda.api.entities.EmbedType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatChannel implements GroupReceiver {

    private final String id;
    private boolean defaultChannel;
    private boolean muted;
    private String format;
    private String formatId;
    private String command;
    private int radius = -1;
    private String world;
    private boolean autoJoin;
    private boolean visibleAnywhere;
    private String discordChannel;
    private List<String> servers;
    private List<UUID> players;

    /**
     * Creates a new chat channel with the given ID.
     * @param id The ID of the channel.
     * @param format The format to use for this channel.
     * @param defaultChannel Whether or not this should be the channel player's join the first time they join the server.
     */
    public ChatChannel(String id, String format, boolean defaultChannel) {
        this.id = id;
        this.format = format;
        this.formatId = "channel-" + id;
        this.defaultChannel = defaultChannel;
        this.players = new ArrayList<>();
        this.servers = new ArrayList<>();
    }

    /**
     * Creates a new chat channel with the given ID.
     * @param id The ID of the channel.
     * @param format The format to use for this channel.
     */
    public ChatChannel(String id, String format) {
        this(id, format, false);
    }

    @Override
    public void send(MessageWrapper messageWrapper) {
        if (this.isMuted() && !(messageWrapper.getSender().hasPermission("rosechat.mute.bypass")))  {
            messageWrapper.getSender().send(RoseChatAPI.getInstance().getLocaleManager().getLocaleMessage("channel-muted"));
            return;
        }

        ComponentBuilder builder = new ComponentBuilder("[Spy] ");
        builder.append(messageWrapper.getComponents());
        for (UUID uuid : RoseChatAPI.getInstance().getDataManager().getChannelSpies()) {
            if (!this.players.contains(uuid)) {
                Player spy = Bukkit.getPlayer(uuid);
                if (spy != null) spy.spigot().sendMessage(builder.create());
            }
        }

        RoseChatAPI api = RoseChatAPI.getInstance();
        if (api.getDiscord() != null) {
            TextChannel textChannel = api.getDiscord().getDestinationTextChannelForGameChannelName(this.getDiscordChannel());
            if (textChannel != null) {
                MessageEmbed messageEmbed = new MessageEmbed(null,
                        "[" + messageWrapper.getPrefix() + "] [" + messageWrapper.getSender().getGroup() + "] " + messageWrapper.getSender().getName() + ": " + messageWrapper.getMessage(),
                        null, EmbedType.RICH, null, 12648430,
                        new MessageEmbed.Thumbnail("https://cravatar.eu/helmavatar/" + messageWrapper.getSender().getName(), "https://cravatar.eu/helmavatar/" + messageWrapper.getSender().getName(), 128, 128),
                        null, null,
                        null, null, null, null);
                textChannel.sendMessage(messageEmbed).queue();
            }
        }

        if (this.isVisibleAnywhere()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                this.sendToPlayer(messageWrapper, player);
            }

            return;
        }

        if (this.getRadius() != -1 && messageWrapper.getSender().isPlayer()) {
            Location playerLocation = messageWrapper.getSender().asPlayer().getLocation();

            if (playerLocation.getWorld() == null) return;
            for (Player player : playerLocation.getWorld().getPlayers()) {
                if (player.getLocation().distance(playerLocation) < this.radius) {
                    this.sendToPlayer(messageWrapper, player);
                }
            }

            return;
        }

        if (this.world != null) {
            World world = Bukkit.getWorld(this.world);

            if (world == null) return;
            for (Player player : world.getPlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                this.sendToPlayer(messageWrapper, player);
            }
        }

        for (String server : this.servers) {
           BungeeListener.sendChannelMessage(this.getId(), server, ComponentSerializer.toString(messageWrapper.getComponents()));
        }

        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                this.sendToPlayer(messageWrapper, player);
            }
        }

        if (messageWrapper.getSender().isPlayer() && !this.players.contains(messageWrapper.getSender().asPlayer().getUniqueId())) {
            Player player = messageWrapper.getSender().asPlayer();
            this.sendToPlayer(messageWrapper, player);
        }
    }

    @Override
    public void sendJson(String json) {
        if (this.isMuted()) return;

        BaseComponent[] components = ComponentSerializer.parse(json);

        if (this.isVisibleAnywhere()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(components);
            }

            return;
        }

        if (this.getRadius() > 0) {
            // hmm...
            return;
        }

        if (this.world != null) {
            World world = Bukkit.getWorld(this.world);

            if (world == null) return;
            for (Player player : world.getPlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(components);
            }
        }

        for (String server : this.servers) {
            BungeeListener.sendChannelMessage(this.getId(), server, ComponentSerializer.toString(components));
        }

        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(components);
            }
        }
    }

    public void sendFromDiscord(MessageWrapper messageWrapper) {
        if (this.isVisibleAnywhere()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(messageWrapper.getComponents());
            }

            return;
        }

        if (this.world != null) {
            World world = Bukkit.getWorld(this.world);

            if (world == null) return;
            for (Player player : world.getPlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(messageWrapper.getComponents());
            }
        }

        for (String server : this.servers) {
            BungeeListener.sendChannelMessage(this.getId(), server, ComponentSerializer.toString(messageWrapper.getComponents()));
        }

        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.spigot().sendMessage(messageWrapper.getComponents());
            }
        }

        ComponentBuilder builder = new ComponentBuilder("[Spy] ");
        builder.append(messageWrapper.getComponents());
        for (UUID uuid : RoseChatAPI.getInstance().getDataManager().getChannelSpies()) {
            if (!this.players.contains(uuid)) {
                Player spy = Bukkit.getPlayer(uuid);
                if (spy != null) spy.spigot().sendMessage(builder.create());
            }
        }
    }

    @Override
    public List<UUID> getMembers() {
        return this.players;
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
     * Gets the ID of the channel.
     * @return The ID of the channel.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Checks if the channel is the default channel.
     * @return True if the channel is the default channel.
     */
    public boolean isDefaultChannel() {
        return this.defaultChannel;
    }

    /**
     * Sets the channel to being the default channel.
     * @param defaultChannel Whether or not the channel should be the default channel.
     */
    public void setDefaultChannel(boolean defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    /**
     * Gets the channel format.
     * @return The channel format.
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * Sets the channel format.
     * @param format The format to use.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the channel format ID.
     * @return The channel format ID.
     */
    public String getFormatId() {
        return this.formatId;
    }

    /**
     * Sets the channel format ID.
     * @param formatId The format ID to use.
     */
    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    /**
     * Gets the radius that messages can be received within.
     * @return The channel radius.
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
     * Gets the world that messages from this channel can be sent and received in.
     * @return The world.
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
     * Whether or not players will automatically join the channel when entering the world.
     * @return True if players will automatically join the channel when entering the world.
     */
    public boolean isAutoJoin() {
        return this.autoJoin;
    }

    /**
     * Sets whether players will automatically join the channel when entering the world.
     * @param autoJoin Whether or not players will automatically join the channel when entering the world.
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
     * Gets the bungee servers the messages will be sent to.
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
     * Is this channel visible anywhere?
     * @return Whether or not this channel is visible anywhere.
     */
    public boolean isVisibleAnywhere() {
        return this.visibleAnywhere;
    }

    /**
     * Sets this channel as visible, meaning that messages will always be received even if a player is in another channel.
     * @param visibleAnywhere Whether or not the channel should be visible anywhere.
     */
    public void setVisibleAnywhere(boolean visibleAnywhere) {
        this.visibleAnywhere = visibleAnywhere;
    }

    /**
     * Gets the command that can be used to access the channel.
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
     * Gets whether or not the channel is muted.
     * @return Whether or not the channel is muted.
     */
    public boolean isMuted() {
        return this.muted;
    }

    /**
     * Sets whether or not the channel is muted.
     * @param muted Whether or not the channel is muted.
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
     * Gets the DiscordSRV channel that the channel is connected to.
     * @return The DiscordSRV channel that the channel is connected to.
     */
    public String getDiscordChannel() {
        return this.discordChannel;
    }
}
