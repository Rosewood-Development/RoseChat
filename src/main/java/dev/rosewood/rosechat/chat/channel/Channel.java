package dev.rosewood.rosechat.chat.channel;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.log.ChannelMessageLog;
import dev.rosewood.rosechat.chat.task.SlowmodeTask;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.MessageRules;
import dev.rosewood.rosechat.message.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class Channel {

    private final ChannelProvider provider;
    protected final List<UUID> members;
    protected final ChannelMessageLog messageLog;
    protected String id;
    protected ChannelSettings settings;
    protected SlowmodeTask slowmodeTask;
    private boolean isMuted;
    private int slowmodeSpeed;

    public Channel(ChannelProvider provider) {
        this.members = new ArrayList<>();
        this.provider = provider;
        this.messageLog = new ChannelMessageLog();
    }

    /**
     * Called when a channel is loaded from a config file.
     */
    public void onLoad(String id, ConfigurationSection config) {
        this.id = id;

        this.settings = ChannelSettings.fromConfig(config);
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
     * @param player The {@link RosePlayer} who is joining the channel.
     * @return True, if the player can join.
     */
    public boolean onLogin(RosePlayer player) {
        // No default implementation.
        return false;
    }

    /**
     * Called when the player enters a world.
     * This is used to check if the player should join the channel when changing world.
     * @param player The {@link RosePlayer} who is changing world.
     * @param from The {@link World} that the player was in.
     * @param to The {@link World} that the player is going to.
     * @return True, if the player should join the channel when entering the world.
     */
    public boolean onWorldJoin(RosePlayer player, World from, World to) {
        // No default implementation.
        return false;
    }

    /**
     * Called when the player leaves a world.
     * This is used to check if the player should leave the channel when changing world.
     * @param player The {@link RosePlayer} who is changing world.
     * @param from The {@link World} that the player was in.
     * @param to The {@link World} that the player is going to.
     * @return True, if the player should leave the channel when leaving the world.
     */
    public boolean onWorldLeave(RosePlayer player, World from, World to) {
        // No default implementation.
        return false;
    }

    /**
     * Called when a player joins the channel.
     * @param player The {@link RosePlayer} who is joining the channel.
     */
    public void onJoin(RosePlayer player) {
        this.members.add(player.getUUID());
    }

    /**
     * Called when a player leaves a channel.
     * @param player The {@link RosePlayer} who is leaving the channel.
     */
    public void onLeave(RosePlayer player) {
        this.members.remove(player.getUUID());
    }

    /**
     * Sends a message to the channel.
     * This automatically finds out who should receive the message and sends it to them.
     * @param options The {@link ChannelMessageOptions} to use when sending the message.
     */
    public abstract void send(ChannelMessageOptions options);

    /**
     * Applies the standard rules to the {@link RoseMessage} and blocks the message if rules were broken.
     * @param message The {@link RoseMessage} to apply rules to.
     * @param input The input string to check.
     * @return {@link MessageRules} applied for the message, or null if blocked.
     */
    protected MessageRules applyRules(RoseMessage message, String input) {
        MessageRules rules = new MessageRules().applyAllFilters();
        MessageRules.RuleOutputs outputs = rules.apply(message, input);


        Bukkit.getScheduler().runTask(RoseChat.getInstance(), () -> {
            for (String command : outputs.getServerCommands())
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("%player%", message.getSender().getRealName()));

            for (String command : outputs.getPlayerCommands())
                Bukkit.dispatchCommand(message.getSender().isPlayer() ? message.getSender().asPlayer() : Bukkit.getConsoleSender(),
                        command.replace("%player%", message.getSender().getRealName()));
        });

        if (outputs.isBlocked()) {
            if (outputs.getWarningMessage() != null) {
                message.getSender().send(outputs.getWarningMessage());
            } else if (outputs.getWarning() != null) {
                outputs.getWarning().send(message.getSender());
            }

            if (Settings.SEND_BLOCKED_MESSAGES_TO_STAFF.get() && outputs.shouldNotifyStaff()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("rosechat.seeblocked")) {
                        RosePlayer rosePlayer = new RosePlayer(player);
                        rosePlayer.sendLocaleMessage("blocked-message",
                                StringPlaceholders.of("player", message.getSender().getName(),
                                        "message", input));
                    }
                }
            }

            return null;
        }

        message.setPlayerInput(outputs.getFilteredMessage());
        return rules;
    }

    /**
     * Called when a player uses a command to join the channel.
     * @param player The {@link RosePlayer} using the command.
     * @return True, if the player can join by using the command.
     */
    public abstract boolean canJoinByCommand(RosePlayer player);

    /**
     * @return A {@link StringPlaceholders.Builder} containing values to be shown in the /chat info command.
     */
    public StringPlaceholders.Builder getInfoPlaceholders() {
        LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
        String trueValue = localeManager.getLocaleMessage("command-chat-info-true");
        String falseValue = localeManager.getLocaleMessage("command-chat-info-false");
        String nullValue = localeManager.getLocaleMessage("command-chat-info-none");

        return this.settings.toPlaceholders(nullValue)
                .add("default", this.getSettings().isDefault() ? trueValue : falseValue)
                .add("members", this.getMemberCount())
                .add("players", this.getMemberCount())
                .add("servers", this.getServers().isEmpty() ? nullValue : this.getServers().toString())
                .add("id", this.getId())
                .add("muted", this.isMuted)
                .add("slowmode", this.slowmodeTask == null ? falseValue : this.slowmodeSpeed);
    }

    /**
     * Checks if a message can be received by a player.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param receiver The {@link RosePlayer} to receive the message.
     * @return True if the receiver can receive the message.
     */
    public boolean canPlayerReceiveMessage(RosePlayer sender, RosePlayer receiver) {
        if (sender.getUUID() != null && receiver.getUUID() != null)
            if (sender.getUUID().equals(receiver.getUUID()))
                return true;

        PlayerData data = receiver.getPlayerData();
        if (data == null)
            return true;

        if (!receiver.hasPermission("rosechat.channel." + this.getId()))
            return false;

        if (data.isChannelHidden(this.getId()))
            return false;

        if (sender.getUUID() != null)
            return !data.getIgnoringPlayers().contains(sender.getUUID());

        return true;
    }

    public void startSlowmode() {
        this.slowmodeTask = new SlowmodeTask(this, this.slowmodeSpeed);
    }

    public void stopSlowmode(boolean clear) {
        if (this.slowmodeTask != null) {
            this.slowmodeTask.stop();
            this.slowmodeTask = null;

            if (clear)
                this.messageLog.getMessages().clear();
        }
    }

    /**
     * Handles slow mode for a message.
     * @param options The {@link ChannelMessageOptions} containing message information.
     * @return True if the message has been handled and slow mode is enabled.
     */
    protected boolean handleSlowmode(ChannelMessageOptions options) {
        if (this.slowmodeTask != null && !options.bypassSlowmode()
                && !options.sender().hasPermission("rosechat.slowmode.bypass." + this.getId())) {
            this.messageLog.addMessage(options);
            return true;
        }

        return false;
    }

    /**
     * Returns a list of members for the given channel.
     * These are the players who have joined the channel, not all the players receiving the message.
     * Use {@link #members} if a player does not need to be specified.
     * A player should be specified when getting a team from a supported plugin.
     * @return A list of UUIDs for the members of the channel.
     */
    public abstract List<UUID> getMembers();

    public Set<Player> getIntendedRecipients(RosePlayer sender, boolean includeSpies) {
        Set<Player> recipients = new HashSet<>();
        this.getMembers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline())
                recipients.add(player);
        });
        return recipients;
    }

    /**
     * @return The amount of members in the channel, this is not always the amount of {@link Channel#getMembers()}.
     */
    public int getMemberCount() {
        return this.members.size();
    }

    /**
     * @return The id of the channel.
     */
    public abstract String getId();

    /**
     * @return A list of servers linked to the channel.
     */
    public abstract List<String> getServers();

    public boolean isMuted() {
        return this.isMuted;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public int getSlowmodeSpeed() {
        return this.slowmodeSpeed;
    }

    public void setSlowmodeSpeed(int slowmodeSpeed) {
        this.slowmodeSpeed = slowmodeSpeed;
    }

    public ChannelProvider getProvider() {
        return this.provider;
    }

    public ChannelMessageLog getMessageLog() {
        return this.messageLog;
    }

    public ChannelSettings getSettings() {
        return this.settings;
    }

}
