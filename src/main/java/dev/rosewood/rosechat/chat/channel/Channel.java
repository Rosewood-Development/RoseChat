package dev.rosewood.rosechat.chat.channel;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Channel {

    private final ChannelProvider provider;
    private boolean defaultChannel;
    private boolean muted;

    // Settings
    private String format;
    private List<String> commands;

    public Channel(ChannelProvider provider) {
        this.provider = provider;
        this.commands = new ArrayList<>();
    }

    /**
     * Called when a channel is loaded from a config file.
     */
    public void onLoad(String id, ConfigurationSection config) {

    }

    /**
     * Called when a channel is loaded.
     */
    public void onLoad() {

    }

    /**
     * Called when a player joins the channel.
     * @param player The {@link Player} is joining the channel.
     */
    public void onJoin(Player player) {

    }

    /**
     * Called when a player leaves a channel.
     * @param player The {@link Player} who is leaving the channel.
     */
    public void onLeave(Player player) {

    }

    /**
     * Called when a message is sent to the channel.
     * @param message The message that was sent.
     */
    public abstract void send(MessageWrapper message);

    /**
     * @param sender The {@link RoseSender} sending the message.
     * @return A list of UUIDs for the members of the channel.
     */
    public abstract List<UUID> getMembers(RoseSender sender);

    public abstract String getId();

    public StringPlaceholders.Builder getInfoPlaceholders(RoseSender sender) {
        return StringPlaceholders.builder()
                .addPlaceholder("default", this.isDefaultChannel())
                .addPlaceholder("muted", this.isMuted())
                .addPlaceholder("members", this.getMembers(sender).size())
                .addPlaceholder("players", this.getMembers(sender).size())
                .addPlaceholder("id", this.getId())
                .addPlaceholder("format", this.getFormat())
                .addPlaceholder("commands", this.getCommands().toString());
    }

    public ChannelProvider getProvider() {
        return this.provider;
    }

    public void registerCommand(String command) {
        this.commands.add(command);
        RoseChat.getInstance().getManager(ChannelManager.class).registerCommand(command);
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
