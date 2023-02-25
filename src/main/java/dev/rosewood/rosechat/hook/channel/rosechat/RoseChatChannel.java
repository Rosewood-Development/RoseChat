package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoseChatChannel extends Channel {

    // Channel Settings
    private int radius;
    private String discordChannel;
    private boolean autoJoin;
    private boolean visibleAnywhere;
    private boolean joinable;
    private boolean keepFormatOverBungee;
    private List<String> worlds;
    private List<String> servers;

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
        if (this.worlds.isEmpty()) return false;

        // No point in joining again if the new world is linked too.
        if (this.worlds.contains(from.getName()) && this.worlds.contains(to.getName())) return false;

        // Join the channel if the world is linked to the channel.
        return this.worlds.contains(to.getName());
    }

    @Override
    public boolean onWorldLeave(Player player, World from, World to) {
        if (this.worlds.isEmpty()) return false;

        // No point in leaving if the new world is linked too.
        if (this.worlds.contains(from.getName()) && this.worlds.contains(to.getName())) return false;

        // Leave the channel if the world is linked to the channel.
        return this.worlds.contains(from.getName());
    }

    @Override
    public void send(RosePlayer sender, String message) {
        // Create the rules for this message.
        MessageRules rules = new MessageRules().applyAllFilters().applySenderChatColor();

        // Parses the first message synchronously
        // Allows for creating a token storage.
        RoseMessage roseMessage = new RoseMessage(sender, this, message);
        roseMessage.applyRules(rules);
        BaseComponent[] parsedConsoleMessage = roseMessage.parse(new RosePlayer(Bukkit.getConsoleSender()), this.getFormat());

        // Send the parsed message to the console
        Bukkit.getConsoleSender().spigot().sendMessage(parsedConsoleMessage);

        // Asynchronously send the message for every player.
        // This allows for the message to be parsed separately for each player.
        for (Player player : Bukkit.getOnlinePlayers()) {
            RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                BaseComponent[] parsedMessage = roseMessage.parse(new RosePlayer(player), this.getFormat());
                player.spigot().sendMessage(parsedMessage);
            });
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
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
                .addPlaceholder("radius", this.radius)
                .addPlaceholder("discord", this.discordChannel)
                .addPlaceholder("auto-join", this.autoJoin)
                .addPlaceholder("visible-anywhere", this.visibleAnywhere)
                .addPlaceholder("joinable", this.joinable)
                .addPlaceholder("keep-format", this.keepFormatOverBungee)
                .addPlaceholder("worlds", this.worlds.toString())
                .addPlaceholder("servers", this.servers.toString());
    }

}
