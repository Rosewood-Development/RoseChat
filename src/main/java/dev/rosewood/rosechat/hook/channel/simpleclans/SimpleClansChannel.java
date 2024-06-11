package dev.rosewood.rosechat.hook.channel.simpleclans;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.events.DisbandClanEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerJoinedClanEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerKickedClanEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.List;

public class SimpleClansChannel extends RoseChatChannel implements Listener {

    private SimpleClansChannelType channelType;

    public SimpleClansChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type"))
            this.channelType = SimpleClansChannelType.valueOf(config.getString("channel-type").toUpperCase());

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;

        if (this.channelType == null)
            this.channelType = SimpleClansChannelType.CLAN;
    }

    // Team Disband
    @EventHandler
    public void onTeamDisband(DisbandClanEvent event) {
        for (ClanPlayer cPlayer : event.getClan().getMembers()) {
            this.onTeamLeaveGeneric(cPlayer.getUniqueId());
        }
    }

    // Team Kick
    @EventHandler
    public void onTeamKick(PlayerKickedClanEvent event) {
        this.onTeamLeaveGeneric(event.getClanPlayer().getUniqueId());
    }

    // Team Join
    @EventHandler
    public void onTeamJoin(PlayerJoinedClanEvent event) {
        if (this.autoJoin) {
            Player player = Bukkit.getPlayer(event.getClanPlayer().getUniqueId());
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

    private boolean hasTeam(Player player) {
        Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUniqueId());
        return clan != null;
    }

    @Override
    public boolean onLogin(Player player) {
        return super.onLogin(player) && this.hasTeam(player);
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer())
            return recipients;

        Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(sender.getUUID());
        if (clan == null)
            return recipients;

        if (this.channelType == SimpleClansChannelType.CLAN) {
            for (ClanPlayer clanPlayer : clan.getMembers()) {
                if (clanPlayer == null)
                    continue;

                Player player = clanPlayer.toPlayer();
                if (player != null && this.getReceiveCondition(sender, player))
                    recipients.add(player);
            }
        } else {
            for (ClanPlayer clanPlayer : clan.getAllAllyMembers()) {
                if (clanPlayer == null)
                    continue;

                Player player = clanPlayer.toPlayer();
                if (player != null && this.getReceiveCondition(sender, player))
                    recipients.add(player);
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
                .add("type", this.channelType.toString().toLowerCase());
    }

    public SimpleClansChannelType getChannelType() {
        return this.channelType;
    }

    public enum SimpleClansChannelType {

        CLAN,
        ALLY

    }

}
