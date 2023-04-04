package dev.rosewood.rosechat.hook.channel.mcmmo;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class McMMOChannel extends RoseChatChannel implements Listener {

    public McMMOChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @EventHandler
    public void onTeamChange(McMMOPartyChangeEvent event) {
        switch (event.getReason()) {
            case DISBANDED_PARTY:
                for (UUID member : PartyAPI.getMembersMap(event.getPlayer()).keySet())
                    this.kick(member);
                break;
            case KICKED_FROM_PARTY:
            case LEFT_PARTY:
                this.kick(event.getPlayer().getUniqueId());
                break;
            case JOINED_PARTY:
                if (this.autoJoin)
                    this.forceJoin(event.getPlayer().getUniqueId());
                break;
            default:
                return;
        }
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();
        if (!sender.isPlayer()) return recipients;

        LinkedHashMap<UUID, String> members = PartyAPI.getMembersMap(sender.asPlayer());
        if (members == null) return recipients;
        for (UUID member : members.keySet()) {
            Player player = Bukkit.getPlayer(member);
            if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        if (!PartyAPI.inParty(player)) return false;
        return super.canJoinByCommand(player);
    }

}
