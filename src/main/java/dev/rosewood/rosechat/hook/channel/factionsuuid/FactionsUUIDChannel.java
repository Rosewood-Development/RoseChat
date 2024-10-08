package dev.rosewood.rosechat.hook.channel.factionsuuid;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
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

public class FactionsUUIDChannel extends RoseChatChannel implements Listener {

    private FactionsChannelType channelType;

    public FactionsUUIDChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type"))
            this.channelType = FactionsChannelType.valueOf(config.getString("channel-type").toUpperCase());

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;

        FactionsPlugin.getInstance().setHandlingChat(RoseChat.getInstance(), true);

        if (this.channelType == null)
            this.channelType = FactionsChannelType.FACTION;
    }

    @EventHandler
    public void onTeamDisband(FactionDisbandEvent event) {
        for (FPlayer player : event.getFaction().getFPlayers()) {
            this.onTeamLeaveGeneric(player.getOfflinePlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onTeamLeave(FPlayerLeaveEvent event) {
        this.onTeamLeaveGeneric(event.getfPlayer().getOfflinePlayer().getUniqueId());
    }

    @EventHandler
    public void onTeamJoin(FPlayerJoinEvent event) {
        if (this.autoJoin) {
            Player player = Bukkit.getPlayer(event.getfPlayer().getOfflinePlayer().getUniqueId());
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
        Faction faction = FPlayers.getInstance().getByPlayer(player.asPlayer()).getFaction();
        return faction != null;
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

        Faction faction = FPlayers.getInstance().getByPlayer(sender.asPlayer()).getFaction();
        if (faction == null)
            return recipients;

        switch (this.channelType) {
            case FACTION: {
                for (FPlayer fPlayer : faction.getFPlayers()) {
                    Player player = fPlayer.getPlayer();
                    if (player == null)
                        continue;

                    RosePlayer rosePlayer = new RosePlayer(player);
                    if (this.getReceiveCondition(sender, rosePlayer))
                        recipients.add(player);
                }

                return recipients;
            }

            case ALLY: {
                for (FPlayer fPlayer : FPlayers.getInstance().getOnlinePlayers()) {
                    if (faction.getRelationTo(fPlayer) != Relation.ALLY)
                        continue;

                    Player player = fPlayer.getPlayer();
                    if (player == null)
                        continue;

                    RosePlayer rosePlayer = new RosePlayer(player);
                    if (this.getReceiveCondition(sender, rosePlayer))
                        recipients.add(player);
                }

                return recipients;
            }

            case TRUCE: {
                for (FPlayer fPlayer : FPlayers.getInstance().getOnlinePlayers()) {
                    if (faction.getRelationTo(fPlayer) != Relation.TRUCE)
                        continue;

                    Player player = fPlayer.getPlayer();
                    if (player == null)
                        continue;

                    RosePlayer rosePlayer = new RosePlayer(player);
                    if (this.getReceiveCondition(sender, rosePlayer))
                        recipients.add(player);
                }

                return recipients;
            }

            case MOD: {
                for (FPlayer fPlayer : faction.getFPlayers()) {
                    if (!fPlayer.getRole().isAtLeast(Role.MODERATOR))
                        continue;

                    Player player = fPlayer.getPlayer();
                    if (player == null)
                        continue;

                    RosePlayer rosePlayer = new RosePlayer(player);
                    if (this.getReceiveCondition(sender, rosePlayer))
                        recipients.add(player);
                }

                return recipients;
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

    public enum FactionsChannelType {

        FACTION,
        ALLY,
        MOD,
        TRUCE

    }

}
