package dev.rosewood.rosechat.hook.channel.marriagemaster;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.DivorcedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.MarriedEvent;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageMasterPlugin;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
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

public class MarriageMasterChannel extends RoseChatChannel implements Listener {

    private final MarriageMasterPlugin marriageMaster;

    public MarriageMasterChannel(ChannelProvider provider) {
        super(provider);
        this.marriageMaster = (MarriageMasterPlugin) Bukkit.getPluginManager().getPlugin("MarriageMaster");

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @EventHandler
    public void onTeamLeave(DivorcedEvent event) {
        if (!event.getPlayer1().isMarried()) {
            this.kick(event.getPlayer1().getUUID());
            this.onTeamLeaveGeneric(event.getPlayer1().getUUID());
        }

        if (!event.getPlayer2().isMarried()) {
            this.kick(event.getPlayer2().getUUID());
            this.onTeamLeaveGeneric(event.getPlayer2().getUUID());
        }
    }

    @EventHandler
    public void onTeamJoin(MarriedEvent event) {
        if (this.autoJoin) {
            this.forceJoin(event.getPlayer1().getUUID());
            this.forceJoin(event.getPlayer2().getUUID());
            RoseChatAPI.getInstance().getLocaleManager().sendMessage(event.getPlayer1().getPlayerOnline(),
                    "command-channel-joined", StringPlaceholders.of("id", this.getId()));
            RoseChatAPI.getInstance().getLocaleManager().sendMessage(event.getPlayer2().getPlayerOnline(),
                    "command-channel-joined", StringPlaceholders.of("id", this.getId()));
        }
    }

    private boolean hasTeam(Player player) {
        return this.marriageMaster.getPlayerData(player).isMarried();
    }

    @Override
    public boolean onLogin(Player player) {
        return super.onLogin(player) && this.hasTeam(player);
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();
        if (!sender.isPlayer()) return recipients;

        MarriagePlayer mp = this.marriageMaster.getPlayerData(sender.getUUID());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (mp.isPartner(player) && this.getReceiveCondition(sender, player)) recipients.add(player);
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        return super.canJoinByCommand(player) && this.hasTeam(player);
    }

}
