package dev.rosewood.rosechat.hook.channel.kingdomsx;

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
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.events.general.KingdomDisbandEvent;
import org.kingdoms.events.members.KingdomJoinEvent;
import org.kingdoms.events.members.KingdomKickEvent;
import org.kingdoms.events.members.KingdomLeaveEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KingdomsXChannel extends RoseChatChannel implements Listener {

    private KingdomsChannelType channelType;

    public KingdomsXChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type"))
            this.channelType = KingdomsChannelType.valueOf(config.getString("channel-type").toUpperCase());

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;

        if (this.channelType == null)
            this.channelType = KingdomsChannelType.KINGDOM;
    }

    @EventHandler
    public void onTeamDisband(KingdomDisbandEvent event) {
        for (UUID uuid : event.getKingdom().getMembers()) {
            this.onTeamLeaveGeneric(uuid);
        }
    }

    @EventHandler
    public void onTeamKick(KingdomKickEvent event) {
        this.onTeamLeaveGeneric(event.getPlayer().getId());
    }

    @EventHandler
    public void onTeamLeave(KingdomLeaveEvent event) {
        this.onTeamLeaveGeneric(event.getPlayer().getId());
    }

    @EventHandler
    public void onTeamJoin(KingdomJoinEvent event) {
        if (this.autoJoin) {
            Player player = Bukkit.getPlayer(event.getPlayer().getId());
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
        Kingdom kingdom = KingdomPlayer.getKingdomPlayer(player.getUUID()).getKingdom();
        return kingdom != null;
    }

    @Override
    public boolean onLogin(RosePlayer player) {
        return super.onLogin(player) && this.hasTeam(player);
    }

    public List<Player> getRecipientsByRelation(RosePlayer sender, Kingdom kingdom, KingdomRelation relation) {
        List<Player> recipients = new ArrayList<>();

        for (Kingdom k : kingdom.getKingdomsWithRelation(relation)) {
            for (UUID uuid : k.getMembers()) {
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
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer())
            return recipients;

        Kingdom kingdom = KingdomPlayer.getKingdomPlayer(sender.getUUID()).getKingdom();
        if (kingdom == null)
            return recipients;

        switch (this.channelType) {
            case KINGDOM: {
                for (UUID uuid : kingdom.getMembers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null)
                        continue;

                    RosePlayer rosePlayer = new RosePlayer(player);
                    if (this.getReceiveCondition(sender, rosePlayer))
                        recipients.add(player);
                }

                return recipients;
            }

            case NATION: {
                if (kingdom.getNation() == null) return recipients;
                for (Kingdom nation : kingdom.getNation().getKingdoms()) {
                    for (UUID uuid : nation.getMembers()) {
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

            case ALLY: {
                recipients.addAll(this.getRecipientsByRelation(sender, kingdom, KingdomRelation.ALLY));
                return recipients;
            }

            case TRUCE: {
                recipients.addAll(this.getRecipientsByRelation(sender, kingdom, KingdomRelation.TRUCE));
                return recipients;
            }

            case ENEMY: {
                recipients.addAll(this.getRecipientsByRelation(sender, kingdom, KingdomRelation.ENEMY));
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

    public enum KingdomsChannelType {

        NATION,
        KINGDOM,
        ALLY,
        TRUCE,
        ENEMY

    }

}
