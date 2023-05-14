package dev.rosewood.rosechat.hook.channel.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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

public class SuperiorSkyblockChannel extends RoseChatChannel implements Listener {

    private SuperiorSkyblockChannelType channelType;

    public SuperiorSkyblockChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type")) this.channelType = SuperiorSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;

        if (this.channelType == null)
            this.channelType = SuperiorSkyblockChannelType.TEAM;
    }

    // Team Disband
    @EventHandler
    public void onTeamDisband(IslandDisbandEvent event) {
        for (SuperiorPlayer sPlayer : event.getIsland().getIslandMembers(true))
            this.kick(sPlayer.getUniqueId());
    }

    // Team Kick
    @EventHandler
    public void onTeamKick(IslandKickEvent event) {
        this.kick(event.getTarget().getUniqueId());
    }

    // Team Leave
    @EventHandler
    public void onTeamLeave(IslandQuitEvent event) {
        this.kick(event.getPlayer().getUniqueId());
    }

    // Team Join
    @EventHandler
    public void onTeamJoin(IslandJoinEvent event) {
        if (this.autoJoin)
            this.forceJoin(event.getPlayer().getUniqueId());
    }

    private boolean hasTeam(Player player) {
        Island island = SuperiorSkyblockAPI.getPlayer(player.getUniqueId()).getIsland();
        return island != null;
    }

    @Override
    public boolean onLogin(Player player) {
        return super.onLogin(player) && this.hasTeam(player);
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();
        if (!sender.isPlayer()) return recipients;

        Island island = SuperiorSkyblockAPI.getPlayer(sender.getUUID()).getIsland();
        if (island == null) return recipients;

        if (this.channelType == SuperiorSkyblockChannelType.TEAM) {
            for (SuperiorPlayer sPlayer : island.getIslandMembers(true)) {
                if (sPlayer == null || !sPlayer.isOnline()) continue;
                Player player = Bukkit.getPlayer(sPlayer.getUniqueId());
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else if (this.channelType == SuperiorSkyblockChannelType.COOP) {
            for (SuperiorPlayer sPlayer : island.getAllPlayersInside()) {
                if (sPlayer == null || !sPlayer.isOnline()) continue;
                Player player = Bukkit.getPlayer(sPlayer.getUniqueId());
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else {
            for (SuperiorPlayer sPlayer : island.getCoopPlayers()) {
                if (sPlayer == null || !sPlayer.isOnline()) continue;
                Player player = Bukkit.getPlayer(sPlayer.getUniqueId());
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        return super.canJoinByCommand(player) && this.hasTeam(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("type", this.channelType.toString().toLowerCase());
    }

    public SuperiorSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum SuperiorSkyblockChannelType {

        LOCAL,
        TEAM,
        COOP

    }

}
