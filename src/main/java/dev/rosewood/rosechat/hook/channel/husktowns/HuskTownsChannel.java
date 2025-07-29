package dev.rosewood.rosechat.hook.channel.husktowns;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.events.MemberJoinEvent;
import net.william278.husktowns.events.MemberLeaveEvent;
import net.william278.husktowns.events.TownDisbandEvent;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HuskTownsChannel extends RoseChatChannel implements Listener {

    public HuskTownsChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;
    }

    // Team Disband
    @EventHandler
    public void onTeamDisband(TownDisbandEvent event) {
        for (UUID uuid : event.getTown().getMembers().keySet()) {
            this.onTeamLeaveGeneric(uuid);
        }
    }

    // Team Kick
    @EventHandler
    public void onTeamKick(MemberLeaveEvent event) {
        this.onTeamLeaveGeneric(event.getPlayer().getUniqueId());
    }

    // Team Join
    @EventHandler
    public void onTeamJoin(MemberJoinEvent event) {
        if (this.autoJoin) {
            Player player = event.getPlayer();
            if (player == null)
                return;

            RosePlayer rosePlayer = new RosePlayer(player);
            Channel currentChannel = rosePlayer.getChannel();
            if (currentChannel == this)
                return;

            if (rosePlayer.switchChannel(this)) {
                RoseChatAPI.getInstance().getLocaleManager().sendMessage(player,
                        "command-channel-joined", StringPlaceholders.of("id", this.getId()));
            }
        }
    }

    private Town getTown(Player player) {
        Optional<Member> member = BukkitHuskTownsAPI.getInstance().getUserTown(player);
        return member.map(Member::town).orElse(null);
    }

    private boolean hasTeam(RosePlayer player) {
        return this.getTown(player.asPlayer()) != null;
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

        Town town = this.getTown(sender.asPlayer());
        if (town == null)
            return recipients;

        for (UUID uuid : town.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            RosePlayer rosePlayer = new RosePlayer(player);
            if (this.getReceiveCondition(sender, rosePlayer))
                recipients.add(player);
        }

        return recipients;
    }

}
