package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
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

public class RoseChatChannel extends Channel {

    // Channel Settings
    protected int radius;
    protected String discordChannel;
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
        this.discordChannel = config.contains("discord") ? config.getString("discord") : null;
        this.autoJoin = config.contains("auto-join") && config.getBoolean("auto-join");
        this.visibleAnywhere = config.contains("visible-anywhere") && config.getBoolean("visible-anywhere");
        this.joinable = config.contains("joinable") && config.getBoolean("joinable");
        this.keepFormatOverBungee = config.contains("keep-format") && config.getBoolean("keep-format");
        this.worlds = config.contains("worlds") ? config.getStringList("worlds") : new ArrayList<>();
        this.servers = config.contains("servers") ? config.getStringList("servers") : new ArrayList<>();
    }

    @Override
    public boolean onLogin(Player player) {
        return this.autoJoin && (this.worlds.isEmpty() || this.worlds.contains(player.getWorld().getName()));
    }

    @Override
    public boolean onWorldJoin(Player player, World from, World to) {
        if (this.worlds.isEmpty() || !this.autoJoin) return false;

        // No point in joining again if the new world is linked too.
        if (this.worlds.contains(from.getName()) && this.worlds.contains(to.getName())) return false;

        // Join the channel if the world is linked to the channel.
        return this.worlds.contains(to.getName());
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
        if (world == null) {
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        } else {
            return world.getPlayers();
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
        // Don't send the message to the sender if it was sent from discord.
        if (receiver.getUUID().equals(message.getSender().getUUID()) && direction == MessageDirection.FROM_DISCORD) return;

        PlayerData receiverData = RoseChatAPI.getInstance().getPlayerData(receiver.getUUID());
        // Don't send the message if the receiver can't receive it.
        if (!this.canReceiveMessage(receiver, receiverData, message.getSender().getUUID())) return;

        // Send the message to the player asynchronously.
        RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
            // If the message is not a json message, parse normally or parse from discord if an id is available.
            if (direction != MessageDirection.FROM_BUNGEE_RAW) {
                receiver.send(discordId == null ? message.parse(receiver, format) : message.parseMessageFromDiscord(receiver, format, discordId));
            } else {
                // Parse the json message.

                // Replace %other placeholders.
                String jsonMessage = message.getMessage();
                if (PlaceholderAPIHook.enabled()) {
                    Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(jsonMessage);
                    while (matcher.find()) {
                        jsonMessage = jsonMessage.replace(matcher.group(), matcher.group().replace("other_", ""));
                    }
                }

                // Serialize the json message and set the components.
                BaseComponent[] parsedMessage = ComponentSerializer.parse(receiver.isPlayer() ? PlaceholderAPIHook.applyPlaceholders(receiver.asPlayer(), jsonMessage) : jsonMessage);
                message.setComponents(parsedMessage);

                // TODO: Update message deletion
                // Call the post parse message event for the correct viewer if the message was sent over bungee
                //PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(message, message.getSender(), MessageDirection.FROM_BUNGEE_SERVER);
                //Bukkit.getPluginManager().callEvent(postParseMessageEvent);
                receiver.send(message.toComponents());
                //receiverData.getMessageLog().addDeletableMessage(new DeletableMessage(message.getUUID(), ComponentSerializer.toString(message.toComponents()), false, discordId));
            }

            // Play the tag sound to the player.
            if (receiver.isPlayer() && message.getTaggedPlayers().contains(receiver.getUUID())) {
                Player player = receiver.asPlayer();
                if (message.getTagSound() != null && (receiverData != null && receiverData.hasTagSounds()))
                    player.playSound(player.getLocation(), message.getTagSound(), 1.0f, 1.0f);
            }
        });
    }

    private void sendToDiscord(RoseMessage message, MessageDirection direction) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message to discord, if not sent from discord.
        // Json messages are unsupported
        if (direction != MessageDirection.FROM_DISCORD) {
            if (direction != MessageDirection.FROM_BUNGEE_RAW && api.getDiscord() != null && this.discordChannel != null) {
                MessageUtils.sendDiscordMessage(message, this, this.discordChannel);
            }
        }
    }

    private void sendToBungee(RoseMessage message, MessageDirection direction) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Send the message over bungee, if the message was not sent from bungee.
        if (direction != MessageDirection.FROM_BUNGEE_SERVER && direction != MessageDirection.FROM_BUNGEE_RAW && api.isBungee()) {
            for (String server : this.servers) {
                if (this.keepFormatOverBungee) {
                    RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                        api.getBungeeManager()
                                .sendChannelMessage(message.getSender(), server, this.getId(), message.getUUID(),
                                        ComponentSerializer.toString(message.parseBungeeMessage(message.getSender(), this.getFormat())));
                    });
                } else {
                    api.getBungeeManager().sendChannelMessage(message.getSender(), server, this.getId(), message.getUUID(), message.getMessage());
                }
            }
        }
    }

    private void send(RoseMessage message, MessageDirection direction, String format, String discordId) {
        this.sendToPlayer(message, message.getSender(), direction, format, discordId);
        this.sendToDiscord(message, direction);
        this.sendToBungee(message, direction);

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
                    if (message.getSender().getUUID().equals(player.getUniqueId())) continue;

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
                        if (message.getSender().getUUID().equals(player.getUniqueId())) continue;

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
                    if (message.getSender().getUUID().equals(player.getUniqueId())) continue;

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
                if (message.getSender().getUUID().equals(spy.getUniqueId())) continue;

                this.sendToPlayer(message, new RosePlayer(spy), direction, Setting.CHANNEL_SPY_FORMAT.getString(), discordId);
            }
        } else {
            if (this.radius != -1 && message.getSender().isPlayer()) {
                // Not Visible Anywhere and Radius - Send to all members in the radius, only if they are in the channel.
                Location senderLocation = message.getSender().asPlayer().getLocation();
                if (senderLocation.getWorld() == null) return;

                // Sender to all members of the channel.
                for (Player player : this.getMemberRecipients(message.getSender(), null)) {
                    if (message.getSender().getUUID().equals(player.getUniqueId())) continue;

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
                    if (message.getSender().getUUID().equals(player.getUniqueId())) continue;

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
                    if (message.getSender().getUUID().equals(player.getUniqueId())) continue;

                    this.sendToPlayer(message, new RosePlayer(player), direction, format, discordId);
                }

                Predicate<Player> condition = player -> !this.members.contains(player.getUniqueId());
                currentSpies.addAll(this.getSpies(condition));
            }

            // Send the message to all the spies who will not receive the message by being in the channel.
            for (Player spy : currentSpies) {
                if (spy == null || message.getSender().getUUID().equals(spy.getUniqueId())) continue;

                this.sendToPlayer(message, new RosePlayer(spy), direction, Setting.CHANNEL_SPY_FORMAT.getString(), discordId);
            }
        }
    }

    @Override
    public void send(RosePlayer sender, String message) {
        // Create the rules for this message.
        MessageRules rules = new MessageRules().applyAllFilters().applySenderChatColor();

        // Parses the first message synchronously
        // Allows for creating a token storage.
        RoseMessage roseMessage = new RoseMessage(sender, this, message);
        roseMessage.applyRules(rules);

        // Check if the message is allowed to be sent.
        if (roseMessage.isBlocked()) {
            if (roseMessage.getFilterType() != null)
                roseMessage.getFilterType().sendWarning(sender);
            return;
        }

        BaseComponent[] parsedConsoleMessage = roseMessage.parse(new RosePlayer(Bukkit.getConsoleSender()), this.getFormat());

        // Send the parsed message to the console
        Bukkit.getConsoleSender().spigot().sendMessage(parsedConsoleMessage);

        // Send the message to the members.
        this.send(roseMessage, MessageDirection.PLAYER_TO_SERVER, this.getFormat(), null);
    }

    @Override
    public void send(RoseMessage message, String discordId) {
        // Send the message from discord, with the correct format.
        this.send(message, MessageDirection.FROM_DISCORD, Setting.DISCORD_TO_MINECRAFT_FORMAT.getString(), discordId);
    }

    @Override
    public void send(RosePlayer sender, String message, UUID messageId) {
        MessageRules rules = new MessageRules().applyAllFilters().applySenderChatColor();
        RoseMessage roseMessage = new RoseMessage(sender, this, message);
        roseMessage.applyRules(rules);
        roseMessage.setUUID(messageId);
        this.send(roseMessage, MessageDirection.FROM_BUNGEE_SERVER, this.getFormat(), null);
    }

    @Override
    public void sendJson(RosePlayer sender, String message, UUID messageId) {
        // Don't apply rules as they've already been applied.
        RoseMessage roseMessage = new RoseMessage(sender, this, message);
        roseMessage.setUUID(messageId);
        this.send(roseMessage, MessageDirection.FROM_BUNGEE_RAW, this.getFormat(), null);
    }

    @Override
    public void flood(String message) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        // Flood the channel on the linked servers.
        if (api.isBungee()) {
            for (String server : this.servers)
                api.getBungeeManager().sendChannelMessage(new RosePlayer("", ""), server, this.getId(), null, message);
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
        return (player.hasPermission("rosechat.channel." + this.getId()) && this.joinable) || player.hasPermission("rosechat.channelbypass");
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("radius", this.radius == -1 ? nullValue : this.radius)
                .addPlaceholder("discord", this.discordChannel == null ? nullValue : this.discordChannel)
                .addPlaceholder("auto-join", this.autoJoin ? trueValue : falseValue)
                .addPlaceholder("visible-anywhere", this.visibleAnywhere ? trueValue : falseValue)
                .addPlaceholder("joinable", this.joinable ? trueValue : falseValue)
                .addPlaceholder("keep-format", this.keepFormatOverBungee ? trueValue : falseValue)
                .addPlaceholder("worlds", this.worlds.isEmpty() ? nullValue : this.worlds.toString())
                .addPlaceholder("servers", this.servers.isEmpty() ? nullValue : this.servers.toString());
    }

}
