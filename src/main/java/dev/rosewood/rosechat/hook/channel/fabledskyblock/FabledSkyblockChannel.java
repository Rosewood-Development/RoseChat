package dev.rosewood.rosechat.hook.channel.fabledskyblock;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.event.island.IslandDeleteEvent;
import com.songoda.skyblock.api.event.island.IslandKickEvent;
import com.songoda.skyblock.api.event.player.PlayerIslandJoinEvent;
import com.songoda.skyblock.api.event.player.PlayerIslandLeaveEvent;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandRole;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FabledSkyblockChannel extends RoseChatChannel implements Listener {

    private FabledSkyblockChannelType channelType;

    public FabledSkyblockChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type"))
            this.channelType = FabledSkyblockChannelType.valueOf(config.getString("channel-type").toUpperCase());

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;

        if (this.channelType == null)
            this.channelType = FabledSkyblockChannelType.TEAM;
    }

    @EventHandler
    public void onTeamDisband(IslandDeleteEvent event) {
        List<UUID> members = new ArrayList<>();
        members.addAll(event.getIsland().getPlayersWithRole(IslandRole.MEMBER));
        members.addAll(event.getIsland().getPlayersWithRole(IslandRole.OPERATOR));
        members.addAll(event.getIsland().getPlayersWithRole(IslandRole.OWNER));

        for (UUID uuid : members) {
            this.onTeamLeaveGeneric(uuid);
        }
    }

    @EventHandler
    public void onTeamKick(IslandKickEvent event) {
        this.onTeamLeaveGeneric(event.getKicked().getUniqueId());
    }

    @EventHandler
    public void onTeamLeave(PlayerIslandLeaveEvent event) {
        this.onTeamLeaveGeneric(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeamJoin(PlayerIslandJoinEvent event) {
        if (this.autoJoin) {
            Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
            if (player == null)
                return;

            RosePlayer rosePlayer = new RosePlayer(player);
            Channel currentChannel = rosePlayer.getPlayerData().getCurrentChannel();
            if (currentChannel == this)
                return;

            if (rosePlayer.switchChannel(this)) {
                RoseChatAPI.getInstance().getLocaleManager().sendMessage(player,
                        "command-channel-joined", StringPlaceholders.of("id", this.getId()));
            }
        }
    }

    private boolean hasTeam(RosePlayer player) {
        Island island = SkyBlockAPI.getIslandManager().getIsland(player.asPlayer());
        return island != null;
    }

    @Override
    public boolean onLogin(RosePlayer player) {
        return super.onLogin(player) && this.hasTeam(player);
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer())
            return recipients;

        Island island = SkyBlockAPI.getIslandManager().getIsland(sender.asPlayer());
        if (island == null)
            return recipients;

        if (this.channelType == FabledSkyblockChannelType.TEAM) {
            List<UUID> members = new ArrayList<>();
            members.addAll(island.getPlayersWithRole(IslandRole.MEMBER));
            members.addAll(island.getPlayersWithRole(IslandRole.OPERATOR));
            members.addAll(island.getPlayersWithRole(IslandRole.OWNER));

            for (UUID uuid : members) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    continue;

                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer))
                    recipients.add(player);
            }
        } else if (this.channelType == FabledSkyblockChannelType.LOCAL) {
            for (Player player : SkyBlockAPI.getIslandManager().getPlayersAtIsland(island)) {
                if (player == null)
                    continue;

                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer))
                    recipients.add(player);
            }
        } else {
            for (UUID uuid : island.getCoopPlayers().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    continue;

                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer))
                    recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(RosePlayer player) {
        return super.canJoinByCommand(player) && this.hasTeam(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders() {
        return super.getInfoPlaceholders()
                .add("type", this.channelType.toString().toLowerCase());
    }

    public FabledSkyblockChannelType getChannelType() {
        return channelType;
    }

    public enum FabledSkyblockChannelType {

        LOCAL,
        TEAM

    }

}
