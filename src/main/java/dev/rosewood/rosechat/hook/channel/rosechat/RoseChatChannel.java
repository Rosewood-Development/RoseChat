package dev.rosewood.rosechat.hook.channel.rosechat;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.api.event.PreParseMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.condition.ConditionalChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class RoseChatChannel extends ConditionalChannel {

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

    // Generic function for when a player leaves a team.
    // This finds the new channel and adds the player to it.
    // Then, sends the player the channel-joined message.
    public void onTeamLeaveGeneric(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Channel newChannel = Channel.findNextChannel(player);
        newChannel.forceJoin(uuid);

        RoseChatAPI.getInstance().getLocaleManager().sendMessage(player,
                "command-channel-joined", StringPlaceholders.of("id", this.getId()));
    }

    @Override
    public boolean onLogin(Player player) {
        return this.getJoinCondition(player) && (this.autoJoin && (this.worlds.isEmpty() || this.worlds.contains(player.getWorld().getName())));
    }

    @Override
    public boolean onWorldJoin(Player player, World from, World to) {
        if (this.worlds.isEmpty() || !this.autoJoin) return false;

        // No point in joining again if the new world is linked too.
        if ((from != null && this.worlds.contains(from.getName())) && this.worlds.contains(to.getName())) return false;

        // Join the channel if the world is linked to the channel.
        return this.getJoinCondition(player) && this.worlds.contains(to.getName());
    }

    @Override
    public boolean onWorldLeave(Player player, World from, World to) {
        if (this.worlds.isEmpty() || !this.autoJoin) return false;

        // No point in leaving if the new world is linked too.
        if (this.worlds.contains(from.getName()) && this.worlds.contains(to.getName())) return false;

        // Leave the channel if the world is linked to the channel.
        return this.worlds.contains(from.getName());
    }

    /**
     * Retrieves a list of players who should receive spy messages.
     * @param condition A {@link Predicate<Player>} to test against, to see if the player should receive a spy message.
     * @return A {@link List<Player>} of players who should receive a spy message.
     */
    public List<Player> getSpies(Predicate<Player> condition) {
        List<UUID> channelSpies = RoseChatAPI.getInstance().getPlayerDataManager().getChannelSpies();
        List<Player> spies = new ArrayList<>();

        for (UUID uuid : channelSpies) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;


            if (condition.test(player))
                spies.add(player);
        }

        return spies;
    }

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
                if (this.getReceiveCondition(sender, player)) recipients.add(player);
            }

            return recipients;
        } else {
            for (Player player : world.getPlayers()) {
                if (this.getReceiveCondition(sender, player)) recipients.add(player);
            }

            return recipients;
        }
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
            if (player != null) recipients.add(player);
        }

        return recipients;
    }

    protected void sendToPlayer(RoseMessage message, RosePlayer receiver, MessageDirection direction, String format, String discordId) {
        PlayerData receiverData = RoseChatAPI.getInstance().getPlayerData(receiver.getUUID());
        // Don't send the message if the receiver can't receive it.
        if (!this.canPlayerReceiveMessage(receiver, receiverData, message.getSender().getUUID())) return;

        // Send the message to the player asynchronously.
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            DebugManager debugManager = RoseChat.getInstance().getManager(DebugManager.class);

            Stopwatch messageTimer;
            if (debugManager.isEnabled()) {
                messageTimer = Stopwatch.createStarted();
            } else {
                messageTimer = null;
            }

            // If the message is not a json message, parse normally, or parse from discord if an id is available.
            MessageOutputs outputs;
            if (direction != MessageDirection.FROM_BUNGEE_RAW) {

                // Call the PreParseMessageEvent and check if the message can be parsed.
                PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(message, receiver, direction);
                Bukkit.getPluginManager().callEvent(preParseMessageEvent);

                if (preParseMessageEvent.isCancelled()) return;
                MessageTokenizerResults<BaseComponent[]> components = discordId == null ? message.parse(receiver, format) : message.parseMessageFromDiscord(receiver, format, discordId);

                PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(message, receiver, direction, components);
                Bukkit.getPluginManager().callEvent(postParseMessageEvent);

                if (postParseMessageEvent.isCancelled()) return;

                outputs = postParseMessageEvent.getMessageComponents().outputs();
                receiver.send(postParseMessageEvent.getMessageComponents().content());

                DeletableMessage deletableMessage = message.createDeletableMessage(
                        ComponentSerializer.toString(postParseMessageEvent.getMessageComponents().content()), discordId
                );
                receiverData.getMessageLog().addDeletableMessage(deletableMessage);
            } else {
                // Parse the json message.

                // Replace %other placeholders.
                String jsonMessage = message.getPlayerInput();
                if (PlaceholderAPIHook.enabled()) {
                    Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(jsonMessage);
                    while (matcher.find()) {
                        jsonMessage = jsonMessage.replace(matcher.group(), matcher.group().replace("other_", ""));
                    }
                }

                // Serialize the json message and set the components.
                BaseComponent[] parsedMessage = ComponentSerializer.parse(receiver.isPlayer() ? PlaceholderAPIHook.applyPlaceholders(receiver.asPlayer(), jsonMessage) : jsonMessage);
                MessageTokenizerResults<BaseComponent[]> components = new MessageTokenizerResults<>(parsedMessage, new MessageOutputs());

                // Call the post parse message event for the correct viewer if the message was sent over bungee
                PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(message, message.getSender(), MessageDirection.PLAYER_TO_SERVER, components);
                Bukkit.getPluginManager().callEvent(postParseMessageEvent);
                parsedMessage = postParseMessageEvent.getMessageComponents().content();
                outputs = postParseMessageEvent.getMessageComponents().outputs();
                receiver.send(parsedMessage);
                receiverData.getMessageLog().addDeletableMessage(new DeletableMessage(message.getUUID(), ComponentSerializer.toString(parsedMessage), false, discordId));
            }

            // Play the tag sound to the player.
            if (receiver.isPlayer() && outputs.getTaggedPlayers().contains(receiver.getUUID())) {
                Player player = receiver.asPlayer();
                if (outputs.getTagSound() != null && (receiverData != null && receiverData.hasTagSounds()))
                    player.playSound(player.getLocation(), outputs.getTagSound(), 1.0f, 1.0f);
            }

            if (debugManager.isEnabled() && debugManager.isTimerEnabled() && messageTimer != null && messageTimer.isRunning()) {
                messageTimer.stop();
                long time = messageTimer.elapsed(TimeUnit.MILLISECONDS);
                debugManager.addMessage(() -> "Parsed Message in: " + time + "ms");
                Bukkit.getConsoleSender().sendMessage(HexUtils.colorify("&eParsed Message in: &c" + time + "&ems"));

                if (debugManager.doOnce()) {
                    debugManager.save();
                    debugManager.setEnabled(false);
                    RoseChatAPI.getInstance().getLocaleManager().sendMessage(Bukkit.getConsoleSender(), "command-debug-off");
                } else {
                    debugManager.addMessage(() -> "\n\n\n");
                }
            }
        });
    }

    private void sendToDiscord(RoseMessage message, MessageDirection direction) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message to discord, if not sent from discord.
        // Json messages are unsupported
        if (direction != MessageDirection.FROM_DISCORD && direction != MessageDirection.FROM_BUNGEE_RAW) {
            if (api.getDiscord() != null && this.getDiscordChannel() != null && Setting.USE_DISCORD.getBoolean()) {
                RoseChat.MESSAGE_THREAD_POOL.execute(() -> MessageUtils.sendDiscordMessage(message, this, this.getDiscordChannel()));
            }
        }
    }

    private void sendToBungee(RoseMessage message, MessageDirection direction) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message over bungee, if the message was not sent from bungee.
        if (direction != MessageDirection.FROM_BUNGEE_SERVER && direction != MessageDirection.FROM_BUNGEE_RAW && api.isBungee() && direction != MessageDirection.FROM_DISCORD) {
            for (String server : this.servers) {
                if (this.keepFormatOverBungee) {
                    RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                        api.getBungeeManager()
                                .sendChannelMessage(message.getSender(), server, this.getId(), message.getUUID(), true,
                                        ComponentSerializer.toString(message.parseBungeeMessage(message.getSender(), this.getFormat())));
                    });
                } else {
                    api.getBungeeManager().sendChannelMessage(message.getSender(), server, this.getId(), message.getUUID(), false, message.getPlayerInput());
                }
            }
        }
    }

    private void send(RoseMessage message, MessageDirection direction, String format, String discordId, MessageRules messageRules) {
        // Send message to the player
        this.sendToPlayer(message, message.getSender(), direction, format, discordId);

        // Send message to Discord
        messageRules.ignoreMessageLogging();
        this.sendToDiscord(message, direction);

        // Send message to Bungee
        this.sendToBungee(message, direction);

        if (direction == MessageDirection.PLAYER_TO_SERVER) {
            RosePlayer consoleReceiver = new RosePlayer(Bukkit.getConsoleSender());

            // Call the PreParseMessageEvent and check if the message can be parsed.
            PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(message, consoleReceiver, direction);
            Bukkit.getPluginManager().callEvent(preParseMessageEvent);

            if (preParseMessageEvent.isCancelled()) return;

            BaseComponent[] parsedConsoleMessage = message.parse(consoleReceiver, this.getFormat()).content();
            Bukkit.getConsoleSender().spigot().sendMessage(parsedConsoleMessage);
        }

        List<Player> currentSpies = new ArrayList<>();

        // Use the settings to decide who to send the message to.
        if (this.visibleAnywhere) {
            if (this.radius != -1 && message.getSender().isPlayer()) {
                // Visible Anywhere + Radius - Send to players in the radius, without them being in the channel.
                // If the radius setting is enabled, and the sender is a player, then send the message.
                Location senderLocation = message.getSender().asPlayer().getLocation();
                if (senderLocation.getWorld() == null) return;

                // Loop through all the players in the world.
                for (Player player : this.getVisibleAnywhereRecipients(message.getSender(), senderLocation.getWorld())) {
                    if (!message.getSender().isConsole() && message.getSender().getUUID().equals(player.getUniqueId())) continue;

                    // Send the message if the player is within the distance.
                    if (player.getLocation().distance(senderLocation) <= this.radius) {
                        this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                    }
                }

                // Allow spies who are not in the same world to see the message.
                Predicate<Player> condition = player -> !player.getWorld().getName().equals(senderLocation.getWorld().getName())
                        || (player.getWorld().getName().equals(senderLocation.getWorld().getName()) && player.getLocation().distance(senderLocation) >= this.radius);
                currentSpies.addAll(this.getSpies(condition));
            } else if (!this.worlds.isEmpty()){
                // Visible Anywhere + World - Send to players in the world, without them being in the channel.
                for (String worldStr : this.worlds) {
                    World world = Bukkit.getWorld(worldStr);
                    if (world == null) continue;

                    // Loop through all the players in the world.
                    for (Player player : this.getVisibleAnywhereRecipients(message.getSender(), world)) {
                        if (!message.getSender().isConsole() && message.getSender().getUUID().equals(player.getUniqueId())) continue;

                        this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                    }
                }

                // Allow spies who are not in the same world to see the message.
                Predicate<Player> condition = player -> !this.worlds.contains(player.getWorld().getName());
                currentSpies.addAll(this.getSpies(condition));
            } else {
                // ONLY Visible Anywhere Enabled - Send to all players.
                List<Player> players = this.getVisibleAnywhereRecipients(message.getSender(), null);

                for (Player player : players) {
                    if (!message.getSender().isConsole() && message.getSender().getUUID().equals(player.getUniqueId())) continue;

                    this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                }

                for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getChannelSpies()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) continue;
                    if (!players.contains(player)) currentSpies.add(player);
                }
            }

            // Send the message to all the spies who will not receive the message by being in the channel.
            for (Player spy : currentSpies) {
                if (spy == null) continue;
                if (!message.getSender().isConsole() && message.getSender().getUUID().equals(spy.getUniqueId())) continue;

                this.sendToPlayer(message, new RosePlayer(spy), direction, Setting.CHANNEL_SPY_FORMAT.getString(), discordId);
            }
        } else {
            if (this.radius != -1 && message.getSender().isPlayer()) {
                // Not Visible Anywhere and Radius - Send to all members in the radius, only if they are in the channel.
                Location senderLocation = message.getSender().asPlayer().getLocation();
                if (senderLocation.getWorld() == null) return;

                // Sender to all members of the channel.
                for (Player player : this.getMemberRecipients(message.getSender(), null)) {
                    if (!message.getSender().isConsole() && message.getSender().getUUID().equals(player.getUniqueId())) continue;

                    if (player.getWorld().getName().equals(senderLocation.getWorld().getName())
                            && player.getLocation().distance(senderLocation) <= this.radius) {
                        this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                    }
                }

                // Allow spies to see the message if they aren't in the world or distance.
                Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId()) ||
                        (this.members.contains(player.getUniqueId()) && (!player.getWorld().getName().equals(senderLocation.getWorld().getName())
                        || (player.getWorld().getName().equals(senderLocation.getWorld().getName()) && player.getLocation().distance(senderLocation) > this.radius)));
                currentSpies.addAll(this.getSpies(condition));
            } else if (!this.worlds.isEmpty()) {
                // Not Visible Anywhere + World - Player must be in the channel AND the world to see the message
                for (Player player : this.getMemberRecipients(message.getSender(), null)) {
                    if (!message.getSender().isConsole() && message.getSender().getUUID().equals(player.getUniqueId())) continue;

                    if (this.worlds.contains(player.getWorld().getName())) {
                        this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                    }
                }


                Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId())
                        || (this.members.contains(player.getUniqueId()) && !this.worlds.contains(player.getWorld().getName()));
                currentSpies.addAll(this.getSpies(condition));
            } else {
                // Not Visible Anywhere - Send to the members
                for (Player player : this.getMemberRecipients(message.getSender(), null)) {
                    if (!message.getSender().isConsole() && message.getSender().getUUID().equals(player.getUniqueId())) continue;

                    this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                }

                Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId());
                currentSpies.addAll(this.getSpies(condition));
            }

            // Send the message to all the spies who will not receive the message by being in the channel.
            for (Player spy : currentSpies) {
                if (spy == null || (message.getSender().isConsole() && message.getSender().getUUID().equals(spy.getUniqueId()))) continue;

                this.sendToPlayer(message, new RosePlayer(spy), direction, Setting.CHANNEL_SPY_FORMAT.getString(), discordId);
            }
        }
    }

    @Override
    public void send(RosePlayer sender, String message) {
        RoseMessage roseMessage = RoseMessage.forChannel(sender, this);

        // Create the rules for this message.
        MessageRules rules = new MessageRules().applyAllFilters();
        MessageRules.RuleOutputs outputs = rules.apply(roseMessage, message);

        // Check if the message is allowed to be sent.
        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(sender);
            return;
        }

        roseMessage.setPlayerInput(outputs.getFilteredMessage());

        // Send the message to the members.
        this.send(roseMessage, MessageDirection.PLAYER_TO_SERVER, this.getFormat(), null, rules);
    }

    @Override
    public void send(RoseMessage message, String discordId) {
        // Send the message from discord, with the correct format.
        this.send(message, MessageDirection.FROM_DISCORD, Setting.DISCORD_TO_MINECRAFT_FORMAT.getString(), discordId, new MessageRules());
    }

    @Override
    public void send(RosePlayer sender, String message, UUID messageId, boolean isJson) {
        RoseMessage roseMessage = RoseMessage.forChannel(sender, this);
        roseMessage.setPlayerInput(message);
        roseMessage.setUUID(messageId);

        if (isJson) {
            this.send(roseMessage, MessageDirection.FROM_BUNGEE_RAW, this.getFormat(), null, new MessageRules());
        } else {
            MessageRules rules = new MessageRules().applyAllFilters();
            this.send(roseMessage, MessageDirection.FROM_BUNGEE_SERVER, this.getFormat(), null, rules);
        }
    }

    @Override
    public void send(String message) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Flood the channel on the linked servers.
        if (api.isBungee()) {
            for (String server : this.servers)
                api.getBungeeManager().sendChannelMessage(new RosePlayer("", ""), server, this.getId(), null, false, message);
        }

        // Send to all players who can see the channel.
        if (this.visibleAnywhere) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                player.sendMessage(message);
            }

            return;
        }

        // Send to all players in the worlds
        if (!this.worlds.isEmpty()) {
            for (String worldStr : this.worlds) {
                World world = Bukkit.getWorld(worldStr);
                if (world == null) continue;

                for (Player player : world.getPlayers()) {
                    if (!player.hasPermission("rosechat.channel." + this.getId())) continue;
                    player.sendMessage(message);
                }

                return;
            }
        }

        // Send to all members.
        for (UUID uuid : this.members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
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
    public boolean canJoinByCommand(Player player) {
        return player.hasPermission("rosechat.channelbypass")
                || (player.hasPermission("rosechat.channel." + this.getId()) && this.joinable && this.getJoinCondition(player));
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .add("radius", this.radius == -1 ? nullValue : this.radius)
                .add("discord", this.getDiscordChannel() == null ? nullValue : this.getDiscordChannel())
                .add("auto-join", this.autoJoin ? trueValue : falseValue)
                .add("visible-anywhere", this.visibleAnywhere ? trueValue : falseValue)
                .add("joinable", this.joinable ? trueValue : falseValue)
                .add("keep-format", this.keepFormatOverBungee ? trueValue : falseValue)
                .add("worlds", this.worlds.isEmpty() ? nullValue : this.worlds.toString())
                .add("servers", this.servers.isEmpty() ? nullValue : this.servers.toString());
    }

}
