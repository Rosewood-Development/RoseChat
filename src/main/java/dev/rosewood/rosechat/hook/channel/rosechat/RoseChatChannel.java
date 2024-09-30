package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.message.PostParseMessageEvent;
import dev.rosewood.rosechat.api.event.message.PreParseMessageEvent;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.condition.ConditionalChannel;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class RoseChatChannel extends ConditionalChannel implements Spyable {

    // Channel Settings
    protected int radius;
    protected boolean autoJoin;
    protected boolean visibleAnywhere;
    protected boolean joinable;
    protected boolean keepFormatOverBungee;
    protected List<String> worlds;
    protected List<String> servers;

    public RoseChatChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        // Set settings specifically for RoseChat Channels
        this.radius = config.contains("radius") ? config.getInt("radius") : -1;
        this.autoJoin = config.contains("auto-join") && config.getBoolean("auto-join");
        this.visibleAnywhere = config.contains("visible-anywhere") && config.getBoolean("visible-anywhere");
        this.joinable = !config.contains("joinable") || config.getBoolean("joinable");
        this.keepFormatOverBungee = config.contains("keep-format") && config.getBoolean("keep-format");
        this.worlds = config.contains("worlds") ? config.getStringList("worlds") : new ArrayList<>();
        this.servers = config.contains("servers") ? config.getStringList("servers") : new ArrayList<>();
    }

     // Auto Join Functions

    @Override
    public boolean onLogin(RosePlayer player) {
        return this.getJoinCondition(player) &&
                (this.autoJoin &&
                        (this.worlds.isEmpty() || this.worlds.contains(player.asPlayer().getWorld().getName())));
    }

    @Override
    public boolean onWorldJoin(RosePlayer player, World from, World to) {
        if (this.worlds.isEmpty() || !this.autoJoin)
            return false;

        // No point in joining again if the new world is linked too.
        if ((from != null && this.worlds.contains(from.getName())) && this.worlds.contains(to.getName()))
            return false;

        // Join the channel if the world is linked to the channel.
        return this.getJoinCondition(player) && this.worlds.contains(to.getName());
    }

    @Override
    public boolean onWorldLeave(RosePlayer player, World from, World to) {
        if (this.worlds.isEmpty() || !this.autoJoin)
            return false;

        // No point in leaving if the new world is linked too.
        if (this.worlds.contains(from.getName()) && this.worlds.contains(to.getName()))
            return false;

        // Leave the channel if the world is linked to the channel.
        return this.worlds.contains(from.getName());
    }

     // Recipient Functions

    /**
     * Retrieves a list of players who should receive messages when visible-anywhere is enabled.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param world The {@link World} to get the players from, will be null if the world is not needed.
     * @return A {@link List<Player>} of recipients.
     */
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();
        if (world == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (this.getReceiveCondition(sender, new RosePlayer(player)))
                    recipients.add(player);
            }
        } else {
            for (Player player : world.getPlayers()) {
                if (this.getReceiveCondition(sender, new RosePlayer(player)))
                    recipients.add(player);
            }
        }

        return recipients;
    }

    /**
     * Retrieves a list of players who should receive messages when visible-anywhere is not enabled.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param world The {@link World} to get the players from, will be null if the world is not needed.
     * @return A {@link List<Player>} of recipients.
     */
    public List<Player> getMemberRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        for (UUID member : this.members) {
            Player player = Bukkit.getPlayer(member);
            if (player != null)
                recipients.add(player);
        }

        return recipients;
    }

    // Send Functions

    @Override
    public void send(ChannelMessageOptions options) {
        if (this.handleSlowmode(options))
            return;

        RoseChatAPI api = RoseChatAPI.getInstance();

        // This message is likely sent from another server.
        if (options.messageId() != null) {
            RoseMessage message = RoseMessage.forChannel(options.sender(), this);
            message.setPlayerInput(options.message());
            message.setUUID(options.messageId());
            message.setPlaceholders(this.getInfoPlaceholders().build());

            if (options.isJson()) {
                this.send(options, message, MessageDirection.SERVER_TO_SERVER_RAW, new MessageRules());
            } else {
                // Apply filters if it was sent from another server.
                MessageRules rules = new MessageRules().applyAllFilters();
                this.send(options, message, MessageDirection.SERVER_TO_SERVER, rules);
            }

            return;
        }

        // This message is likely sent from discord to minecraft.
        if (options.wrapper() != null) {
            this.send(options, options.wrapper(), MessageDirection.DISCORD_TO_MINECRAFT, new MessageRules());
            return;
        }

        // If there is no sender, the message should be handled differently.
        if (options.sender() == null) {
            if (api.isBungee())
                this.sendStringToServers(options.message());

            if (this.visibleAnywhere) {
                this.sendStringToVisibleAnywhere(options.message());
            } else if (!this.worlds.isEmpty()) {
                this.sendStringToWorlds(options.message());
            } else {
                this.sendStringToMembers(options.message());
            }

            return;
        }

        // Handle messages to be sent on this server.
        RoseMessage message = RoseMessage.forChannel(options.sender(), this);
        message.setPlaceholders(this.getInfoPlaceholders().build());

        // Apply the rules for this message or return if the message was blocked.
        MessageRules rules = this.applyRules(message, options.message());
        if (rules == null)
            return;

        this.send(options, message, MessageDirection.PLAYER_TO_SERVER, rules);
    }

    // Send To Functions

    private void sendToPlayer(RoseMessage message, RosePlayer receiver, MessageDirection direction,
                              String format, String discordId) {
        // Don't send the message if the receiver can't receive it.
        if (!this.canPlayerReceiveMessage(message.getSender(), receiver))
            return;

        // Send the message to the player asynchronously.
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            MessageOutputs outputs;

            // If the message is a JSON message, parse it before sending.
            if (direction == MessageDirection.SERVER_TO_SERVER_RAW) {
                String jsonMessage = this.applyJSONPlaceholders(message.getPlayerInput());
                MessageTokenizerResults<BaseComponent[]> parsedMessage = this.parseJSONMessage(receiver, jsonMessage);

                PostParseMessageEvent event = new PostParseMessageEvent(message, receiver,
                        MessageDirection.SERVER_TO_SERVER_RAW, parsedMessage);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return;

                parsedMessage = event.getComponents();
                outputs = event.getComponents().outputs();

                receiver.send(parsedMessage.content());
                if (receiver.getPlayerData() != null)
                    receiver.getPlayerData().getMessageLog().addDeletableMessage(
                            new DeletableMessage(message.getUUID(), ComponentSerializer.toString(parsedMessage.outputs()),
                                    false, null, this.getId()));
            } else {
                PreParseMessageEvent preParseEvent = new PreParseMessageEvent(message, receiver, direction);
                Bukkit.getPluginManager().callEvent(preParseEvent);

                if (preParseEvent.isCancelled())
                    return;

                MessageTokenizerResults<BaseComponent[]> components = discordId == null ?
                        message.parse(receiver, format) : message.parseMessageFromDiscord(receiver, format, discordId);

                PostParseMessageEvent postParseEvent = new PostParseMessageEvent(message, receiver, direction, components);
                Bukkit.getPluginManager().callEvent(postParseEvent);

                if (postParseEvent.isCancelled())
                    return;

                outputs = postParseEvent.getComponents().outputs();

                receiver.send(postParseEvent.getComponents().content());

                DeletableMessage deletableMessage = message.createDeletableMessage(
                        ComponentSerializer.toString(postParseEvent.getComponents().content()), discordId);

                if (receiver.getPlayerData() != null)
                    receiver.getPlayerData().getMessageLog().addDeletableMessage(deletableMessage);
            }

            if (!receiver.isPlayer())
                return;

            if (!outputs.getTaggedPlayers().contains(receiver.getUUID()))
                return;

            if ((receiver.getPlayerData() != null && !receiver.getPlayerData().hasTagSounds()) || outputs.getTagSound() == null)
                return;

            Player player = receiver.asPlayer();
            player.playSound(player.getLocation(), outputs.getTagSound(), 1.0f, 1.0f);
        });
    }

    private void sendToDiscord(RoseMessage message, MessageDirection direction) {
        if (direction == MessageDirection.SERVER_TO_SERVER && !this.getSettings().shouldSendBungeeToDiscord())
            return;

        if (direction == MessageDirection.DISCORD_TO_MINECRAFT || direction == MessageDirection.SERVER_TO_SERVER_RAW)
            return;

        RoseChatAPI api = RoseChatAPI.getInstance();
        if (api.getDiscord() == null || this.getSettings().getDiscord() == null || !Settings.USE_DISCORD.get())
            return;

        RoseChat.MESSAGE_THREAD_POOL.execute(() ->
                api.getDiscord().sendMessage(message, this, this.getSettings().getDiscord()));
    }

    private void sendToBungee(RoseMessage message, MessageDirection direction) {
        if (direction == MessageDirection.SERVER_TO_SERVER || direction == MessageDirection.SERVER_TO_SERVER_RAW
                || direction == MessageDirection.DISCORD_TO_MINECRAFT)
            return;

        RoseChatAPI api = RoseChatAPI.getInstance();
        if (!api.isBungee())
            return;

        for (String server : this.servers) {
            if (this.keepFormatOverBungee) {
                RoseChat.MESSAGE_THREAD_POOL.execute(() ->
                        api.getBungeeManager().sendChannelMessage(message.getSender(), server, this.getId(), message.getUUID(), true,
                                ComponentSerializer.
                                        toString(message.parseBungeeMessage(message.getSender(),
                                                this.getSettings().getFormats().get("chat")))));
            } else {
                api.getBungeeManager().sendChannelMessage(message.getSender(), server, this.getId(),
                        message.getUUID(), false, message.getPlayerInput());
            }
        }
    }

    private void sendToConsole(RoseMessage message, MessageDirection direction, String format, String discordId) {
        if (direction != MessageDirection.PLAYER_TO_SERVER && direction != MessageDirection.DISCORD_TO_MINECRAFT)
            return;

        RosePlayer console = new RosePlayer(Bukkit.getConsoleSender());

        PreParseMessageEvent preParseEvent = new PreParseMessageEvent(message, console, direction);
        Bukkit.getPluginManager().callEvent(preParseEvent);
        if (preParseEvent.isCancelled())
            return;

        MessageTokenizerResults<BaseComponent[]> components = direction == MessageDirection.PLAYER_TO_SERVER ?
                message.parse(console, format) :
                message.parseMessageFromDiscord(console, format, discordId);

        PostParseMessageEvent postParseEvent = new PostParseMessageEvent(message, console, direction, components);
        Bukkit.getPluginManager().callEvent(postParseEvent);
        if (postParseEvent.isCancelled())
            return;

        components = postParseEvent.getComponents();

        console.send(components.content());
    }

    private void sendToVisibleAnywhere(RoseMessage message, MessageDirection direction, String format, String discordId) {
        RosePlayer sender = message.getSender();

        // If this channel is visible anywhere and has a radius,
        // it should send the message to players in the radius without them joining.
        // Radius messages can only be sent by players.
        if (this.radius != -1 && sender.isPlayer()) {
            Location location = sender.asPlayer().getLocation();
            if (location.getWorld() == null)
                return;

            // Loop through all the players in the world and check if they are within distance.
            for (Player player : this.getVisibleAnywhereRecipients(sender, location.getWorld())) {
                // Avoid sending the message to the sender.
                if (sender.isPlayer() && sender.getUUID().equals(player.getUniqueId()))
                    continue;

                // Send the message if the player is in distance.
                if (player.getLocation().distance(location) <= this.radius)
                    this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
            }

            // Allow spies who are not in the same world to see the message.
            Predicate<Player> condition = player ->
                    !player.getWorld().getName().equals(location.getWorld().getName())
                            || (player.getWorld().getName().equals(location.getWorld().getName())
                                && player.getLocation().distance(location) >= this.radius);
            this.sendToSpies(condition, message, direction);
            return;
        }

        // If radius is not enabled, send to all players.
        for (Player player : this.getVisibleAnywhereRecipients(message.getSender(), null)) {
            // Avoid sending the message to the sender.
            if (sender.isPlayer() && sender.getUUID().equals(player.getUniqueId()))
                continue;

            this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
        }

        this.sendToSpies(null, message, direction);
    }

    private void sendToChannelMembers(RoseMessage message, MessageDirection direction, String format, String discordId) {
        RosePlayer sender = message.getSender();

        // Send the message to all the channel members in the radius.
        if (this.radius != -1 && sender.isPlayer()) {
            Location location = sender.asPlayer().getLocation();
            if (location.getWorld() == null)
                return;

            for (Player player : this.getMemberRecipients(sender, null)) {
                if (sender.isPlayer() && sender.getUUID().equals(player.getUniqueId()))
                    continue;

                if (!player.getWorld().getName().equals(location.getWorld().getName()))
                    continue;

                if (player.getLocation().distance(location) <= this.radius)
                    this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
            }

            // Allow spies who are not in the same world to see the message.
            Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId())
                    || (this.members.contains(player.getUniqueId()) &&
                            (!player.getWorld().getName().equals(location.getWorld().getName())
                            || (player.getWorld().getName().equals(location.getWorld().getName())
                                    && player.getLocation().distance(location) > this.radius)));
            this.sendToSpies(condition, message, direction);
        } else if (!this.worlds.isEmpty()) {
            // The player must be in the channel and a world to receive the message.
            for (Player player : this.getMemberRecipients(sender, null)) {
                if (sender.isPlayer() && sender.getUUID().equals(player.getUniqueId()))
                    continue;

                if (!this.worlds.contains(player.getWorld().getName()))
                    continue;

                this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
            }

            Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId())
                    || (this.members.contains(player.getUniqueId())
                    && !this.worlds.contains(player.getWorld().getName()));
            this.sendToSpies(condition, message, direction);
        } else {
            // No other conditions to worry about, send directly to all members.
            for (Player player : this.getMemberRecipients(sender, null)) {
                if (sender.isPlayer() && sender.getUUID().equals(player.getUniqueId()))
                    continue;

                this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
            }

            Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId());
            this.sendToSpies(condition, message, direction);
        }
    }

    private void sendToSpies(Predicate<Player> condition, RoseMessage message, MessageDirection direction) {
        List<Player> spies = this.getSpies(condition);
        for (Player spy : spies) {
            if (spy == null)
                continue;

            if (this.members.contains(spy.getUniqueId()))
                continue;

            if (new RosePlayer(spy).getChannel() == this)
                continue;

            // Avoid sending the message to the sender.
            if (message.getSender().getUUID() != null && message.getSender().getUUID().equals(spy.getUniqueId()))
                continue;

            this.sendToPlayer(message, new RosePlayer(spy), direction, Settings.CHANNEL_SPY_FORMAT.get(), null);
        }
    }

    private void send(ChannelMessageOptions options, RoseMessage message, MessageDirection direction, MessageRules rules) {
        // Use the format provided in the options if found, or use the "chat" format in the channel settings.
        String format = options.format() != null ? options.format() : this.getSettings().getFormats().get("chat");

        // Send the message to the person who sent it.
        this.sendToPlayer(message, message.getSender(), direction, format, options.discordId());

        // Disable spam filter for further messages.
        rules.ignoreMessageLogging();

        this.sendToDiscord(message, direction);
        this.sendToBungee(message, direction);

        // Send the message to the console if it should be sent in chat.
        this.sendToConsole(message, direction, format, options.discordId());

        // Send to all visible anywhere recipients.
        if (this.visibleAnywhere) {
           this.sendToVisibleAnywhere(message, direction, format, options.discordId());
        } else {
            this.sendToChannelMembers(message, direction, format, options.discordId());
        }
    }

    // JSON Functions
    private String applyJSONPlaceholders(String message) {
        String output = message;

        if (PlaceholderAPIHook.enabled()) {
            Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(message);
            while (matcher.find())
                output = output.replace(matcher.group(), matcher.group().replace("%other_", ""));
        }

        return output;
    }

    private MessageTokenizerResults<BaseComponent[]> parseJSONMessage(RosePlayer receiver, String json) {
        BaseComponent[] parsed = ComponentSerializer.parse(receiver.isPlayer() ?
                PlaceholderAPIHook.applyPlaceholders(receiver.asPlayer(), json) :
                json);

        return new MessageTokenizerResults<>(parsed, new MessageOutputs());
    }

    // Send String Functions

    /**
     * Directly sends a string to the connected servers.
     * @param message The message to send.
     */
    private void sendStringToServers(String message) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        for (String server : this.servers)
            api.getBungeeManager().sendChannelMessage(new RosePlayer("", ""), server, this.getId(), null, false, message);
    }

    /**
     * Directly sends a string to visible-anywhere recipients.
     * @param message The message to send.
     */
    private void sendStringToVisibleAnywhere(String message) {
        // We have to assume this should be directed at all players, as no sender could be provided.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("rosechat.channel." + this.getId()))
                continue;

            player.sendMessage(message);
        }
    }

    /**
     * Directly sends a string to players in the worlds.
     * @param message The message to send.
     */
    private void sendStringToWorlds(String message) {
        for (String worldStr : this.worlds) {
            World world = Bukkit.getWorld(worldStr);
            if (world == null)
                continue;

            for (Player player : world.getPlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId()))
                    continue;

                player.sendMessage(message);
            }
        }
    }

    /**
     * Directly sends a string to channel members.
     * @param message The message to send.
     */
    private void sendStringToMembers(String message) {
        for (UUID uuid : this.members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null)
                player.sendMessage(message);
        }
    }

    /**
     * Generic function for when a player leaves a team.
     * This finds the new channel and adds the player to it.
     */
    public void onTeamLeaveGeneric(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        RosePlayer rosePlayer = new RosePlayer(player);
        Channel currentChannel = rosePlayer.getChannel();
        if (currentChannel != this)
            return;

        Channel channel = rosePlayer.findChannel();
        boolean success = rosePlayer.switchChannel(channel);

        if (!success)
            return;

        String joinMessage = channel.getSettings().getFormats().get("join-message");
        if (joinMessage != null)
            rosePlayer.send(RoseChatAPI.getInstance().parse(rosePlayer, rosePlayer, joinMessage));
        else
            rosePlayer.sendLocaleMessage("command-channel-joined",
                    StringPlaceholders.of("id", channel.getId()));
    }

    @Override
    public List<UUID> getMembers() {
        return this.members;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List<String> getServers() {
        return this.servers;
    }

    @Override
    public boolean canJoinByCommand(RosePlayer player) {
        return player.hasPermission("rosechat.channelbypass")
                || (player.hasPermission("rosechat.channel." + this.getId()) && this.joinable && this.getJoinCondition(player));
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders() {
        LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
        String trueValue = localeManager.getLocaleMessage("command-chat-info-true");
        String falseValue = localeManager.getLocaleMessage("command-chat-info-false");
        String nullValue = localeManager.getLocaleMessage("command-chat-info-none");

        return super.getInfoPlaceholders()
                .add("radius", this.radius == -1 ? nullValue : this.radius)
                .add("discord", this.getSettings().getDiscord() == null ? nullValue : this.getSettings().getDiscord())
                .add("auto-join", this.autoJoin ? trueValue : falseValue)
                .add("visible-anywhere", this.visibleAnywhere ? trueValue : falseValue)
                .add("joinable", this.joinable ? trueValue : falseValue)
                .add("keep-format", this.keepFormatOverBungee ? trueValue : falseValue)
                .add("worlds", this.worlds.isEmpty() ? nullValue : this.worlds.toString())
                .add("servers", this.servers.isEmpty() ? nullValue : this.servers.toString());
    }

}
