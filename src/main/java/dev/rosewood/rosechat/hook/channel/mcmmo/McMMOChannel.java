package dev.rosewood.rosechat.hook.channel.mcmmo;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOChannel extends RoseChatChannel implements Listener {

    public McMMOChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;
    }

    @EventHandler
    public void onTeamChange(McMMOPartyChangeEvent event) {
        switch (event.getReason()) {
            case DISBANDED_PARTY:
                for (UUID member : PartyAPI.getMembersMap(event.getPlayer()).keySet()) {
                    this.onTeamLeaveGeneric(member);
                }
                break;
            case KICKED_FROM_PARTY:
            case LEFT_PARTY:
                this.onTeamLeaveGeneric(event.getPlayer().getUniqueId());
                break;
            case JOINED_PARTY:
                if (this.autoJoin) {
                    Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
                    if (player == null)
                        return;

                    RosePlayer rosePlayer = new RosePlayer(player);
                    Channel currentChannel = rosePlayer.getPlayerData().getCurrentChannel();
                    if (currentChannel == this)
                        return;

                    RoseChatAPI.getInstance().getLocaleManager().sendMessage(event.getPlayer(),
                            "command-channel-joined", StringPlaceholders.of("id", this.getId()));
                }
                break;
            default:
                return;
        }
    }

    private boolean hasTeam(RosePlayer player) {
        return PartyAPI.inParty(player.asPlayer());
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

        LinkedHashMap<UUID, String> members = PartyAPI.getMembersMap(sender.asPlayer());
        if (members == null)
            return recipients;

        for (UUID member : members.keySet()) {
            Player player = Bukkit.getPlayer(member);
            if (player == null)
                continue;

            RosePlayer rosePlayer = new RosePlayer(player);
            if (this.getReceiveCondition(sender, rosePlayer))
                recipients.add(player);
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(RosePlayer player) {
        return super.canJoinByCommand(player) && this.hasTeam(player);
    }

}
