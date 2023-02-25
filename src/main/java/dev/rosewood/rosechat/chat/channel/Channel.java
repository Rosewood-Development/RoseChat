package dev.rosewood.rosechat.chat.channel;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Channel {

    private final ChannelProvider provider;
    protected String id;
    protected final List<UUID> members;
    private boolean defaultChannel;
    private boolean muted;

    // Settings
    private String format;
    private List<String> commands;

    public Channel(ChannelProvider provider) {
        this.members = new ArrayList<>();
        this.provider = provider;
        this.commands = new ArrayList<>();
    }

    /**
     * Called when a channel is loaded from a config file.
     */
    public void onLoad(String id, ConfigurationSection config) {
        this.id = id;
        if (config.contains("default") && config.getBoolean("default")) this.setDefault();
        if (config.contains("format")) this.setFormat(config.getString("format"));
        if (config.contains("commands")) this.setCommands(config.getStringList("commands"));

        for (String command : this.commands)
            this.registerCommand(command);
    }

    /**
     * Called when a channel is loaded.
     */
    public void onLoad() {
        // No default implementation.
    }

    /**
     * Called when the player joins the server.
     * This is used to check if the player should join the channel when logging in
     * @param player The {@link Player} who is joining the channel.
     * @return True if the player can join.
     */
    public boolean onLogin(Player player) {
        // No default implementation.
        return true;
    }

    /**
     * Called when the player enters a world.
     * This is used to check if the player should join the channel when changing world.
     * @param player The {@link Player} who is changing world.
     * @param from The {@link World} that the player was in.
     * @param to The {@link World} that the player is going to.
     * @return True if the player should join the channel when entering the world.
     */
    public boolean onWorldJoin(Player player, World from, World to) {
        // No default implementation.
        return true;
    }

    /**
     * Called when the player leaves a world.
     * This is used to check if the player should join the channel when changing world.
     * @param player The {@link Player} who is changing world.
     * @param from The {@link World} that the player was in.
     * @param to The {@link World} that the player is going to.
     * @return True if the player should leave the channel when leaving the world.
     */
    public boolean onWorldLeave(Player player, World from, World to) {
        // No default implementation.
        return true;
    }

    /**
     * Called when a player joins the channel.
     * @param player The {@link Player} who is joining the channel.
     */
    public void onJoin(Player player) {
        this.members.add(player.getUniqueId());
    }

    /**
     * Called when a player leaves a channel.
     * @param player The {@link Player} who is leaving the channel.
     */
    public void onLeave(Player player) {
        this.members.remove(player.getUniqueId());
    }

    /**
     * Called when a message is sent to the channel.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param message The message to be sent.
     */
    public abstract void send(RosePlayer sender, String message);

    /**
     * @param sender The {@link RosePlayer} sending the message.
     * @return A list of UUIDs for the members of the channel.
     */
    public abstract List<UUID> getMembers(RosePlayer sender);

    /**
     * @return The id of the channel.
     */
    public abstract String getId();

    /**
     * @return A list of servers linked to the channel.
     */
    public abstract List<String> getServers();

    /**
     * Called when a player uses a command to join the channel.
     * @param player The {@link Player} using the command.
     * @return True if the player can join by using the command.
     */
    public abstract boolean canJoinByCommand(Player player);

    /**
     * @param sender The {@link RosePlayer} getting the info.
     * @param trueValue A value representing 'true', e.g. &aTrue
     * @param falseValue A value representing 'false', e.g. &cFalse
     * @param nullValue A value representing 'null', e.g. &eNone
     * @return A {@link StringPlaceholders.Builder} containing values to be shown in the /chat info command.
     */
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return StringPlaceholders.builder()
                .addPlaceholder("default", this.isDefaultChannel() ? trueValue : falseValue)
                .addPlaceholder("muted", this.isMuted() ? trueValue : falseValue)
                .addPlaceholder("members", this.getMembers(sender).isEmpty() ? nullValue : this.getMembers(sender).size())
                .addPlaceholder("players", this.getMembers(sender).isEmpty() ? nullValue : this.getMembers(sender).size())
                .addPlaceholder("id", this.getId())
                .addPlaceholder("format", this.getFormat() == null ? nullValue : this.getFormat())
                .addPlaceholder("commands", this.commands.isEmpty() ? nullValue : this.getCommands().toString());
    }

    public ChannelProvider getProvider() {
        return this.provider;
    }

    public void registerCommand(String command) {
        RoseChat.getInstance().getManager(ChannelManager.class).registerCommand(command);
    }

    /**
     * Floods the channel with the specified message.
     * @param message The message to use
     */
    public void flood(String message) {
        // No default implementation.
    }

    public boolean isDefaultChannel() {
        return this.defaultChannel;
    }

    public void setDefault() {
        RoseChat.getInstance().getManager(ChannelManager.class).setDefaultChannel(this);
        this.defaultChannel = true;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

}
