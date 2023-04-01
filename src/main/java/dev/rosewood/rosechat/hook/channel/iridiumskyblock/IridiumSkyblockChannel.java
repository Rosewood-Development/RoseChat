package dev.rosewood.rosechat.hook.channel.iridiumskyblock;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;
import com.iridium.iridiumskyblock.api.UserJoinEvent;
import com.iridium.iridiumskyblock.api.UserKickEvent;
import com.iridium.iridiumskyblock.api.UserLeaveEvent;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandTrusted;
import com.iridium.iridiumskyblock.database.User;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class IridiumSkyblockChannel extends RoseChatChannel implements Listener {

    private IridiumSkyblockChannelType channelType;

    public IridiumSkyblockChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type")) this.channelType = IridiumSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @EventHandler
    public void onTeamDisband(IslandDeleteEvent event) {
        for (User user : event.getIsland().getMembers())
            this.kick(user.getUuid());
    }

    @EventHandler
    public void onTeamKick(UserKickEvent event) {
        this.kick(event.getUser().getUuid());
    }

    @EventHandler
    public void onTeamLeave(UserLeaveEvent event) {
        this.kick(event.getUser().getUuid());
    }

    @EventHandler
    public void onTeamJoin(UserJoinEvent event) {
        if (this.autoJoin)
            this.forceJoin(event.getUser().getUuid());
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        User user = IridiumSkyblockAPI.getInstance().getUser(sender.asPlayer());
        if (!user.getIsland().isPresent()) return recipients;

        Island island = user.getIsland().get();

        if (this.channelType == IridiumSkyblockChannelType.TEAM) {
            for (User member : island.getMembers()) {
                if (member == null) continue;

                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else if (this.channelType == IridiumSkyblockChannelType.LOCAL) {
            for (User member : IridiumSkyblock.getInstance().getIslandManager().getPlayersOnIsland(island)) {
                if (member == null) continue;

                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else {
            for (IslandTrusted it : IridiumSkyblock.getInstance().getDatabaseManager().getIslandTrustedTableManager().getEntries(island)) {
                User trusted = it.getUser();
                if (trusted == null) continue;

                Player player = Bukkit.getPlayer(trusted.getUuid());
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        User user = IridiumSkyblockAPI.getInstance().getUser(player);
        if (!user.getIsland().isPresent()) return false;

        return super.canJoinByCommand(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("type", this.channelType.toString().toLowerCase());
    }

    public IridiumSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum IridiumSkyblockChannelType {

        LOCAL,
        TEAM,
        TRUSTED

    }

}
