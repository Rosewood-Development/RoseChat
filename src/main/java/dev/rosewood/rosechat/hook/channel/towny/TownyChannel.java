package dev.rosewood.rosechat.hook.channel.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.town.TownKickEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.manager.LocaleManager;
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

public class TownyChannel extends RoseChatChannel implements Listener {

    private TownyChannelType channelType;
    private boolean useAllies;

    public TownyChannel(ChannelProvider provider) {
        super(provider);

        Bukkit.getPluginManager().registerEvents(this, RoseChat.getInstance());
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type"))
            this.channelType = TownyChannelType.valueOf(config.getString("channel-type").toUpperCase());

        if (config.contains("use-allies"))
            this.useAllies = config.getBoolean("use-allies");

        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;

        if (this.channelType == null)
            this.channelType = TownyChannelType.TOWN;
    }

    // Team Disband
    @EventHandler
    public void onTeamDelete(DeleteTownEvent event) {
        Town town = TownyAPI.getInstance().getTown(event.getTownUUID());
        if (town != null && this.channelType == TownyChannelType.TOWN) {
            for (Resident resident : town.getResidents()) {
                this.onTeamLeaveGeneric(resident.getUUID());
            }
        }
    }

    // Team Kick
    @EventHandler
    public void onTeamKick(TownKickEvent event) {
        this.onTeamLeaveGeneric(event.getKickedResident().getUUID());
    }

    // Team Leave
    @EventHandler
    public void onTeamLeave(TownLeaveEvent event) {
        this.onTeamLeaveGeneric(event.getResident().getUUID());
    }

    // Team Join
    @EventHandler
    public void onTeamJoin(TownAddResidentEvent event) {
        if (this.autoJoin) {
            Player player = Bukkit.getPlayer(event.getResident().getUUID());
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
        if (!player.isPlayer())
            return false;

        Town town = TownyAPI.getInstance().getTown(player.asPlayer());
        Nation nation = TownyAPI.getInstance().getNation(player.asPlayer());

        return (this.channelType == TownyChannelType.TOWN && town != null) || (this.channelType == TownyChannelType.NATION && nation != null);
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

        if (this.channelType == TownyChannelType.TOWN) {
            Town town = TownyAPI.getInstance().getTown(sender.asPlayer());
            if (town == null)
                return recipients;

            for (Resident resident : town.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getUUID());
                if (player == null)
                    continue;

                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer))
                    recipients.add(player);
            }

            if (this.useAllies) {
                for (Town ally : town.getAllies()) {
                    for (Resident resident : ally.getResidents()) {
                        Player player = Bukkit.getPlayer(resident.getUUID());
                        if (player == null)
                            continue;

                        RosePlayer rosePlayer = new RosePlayer(player);
                        if (this.getReceiveCondition(sender, rosePlayer))
                            recipients.add(player);
                    }
                }
            }
        } else {
            Nation nation = TownyAPI.getInstance().getNation(sender.asPlayer());
            if (nation == null)
                return recipients;

            for (Resident resident : nation.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getUUID());
                if (player == null)
                    continue;

                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer))
                    recipients.add(player);
            }

            if (this.useAllies) {
                for (Nation ally : nation.getAllies()) {
                    for (Resident resident : ally.getResidents()) {
                        Player player = Bukkit.getPlayer(resident.getUUID());
                        if (player == null)
                            continue;

                        RosePlayer rosePlayer = new RosePlayer(player);
                        if (this.getReceiveCondition(sender, rosePlayer))
                            recipients.add(player);
                    }
                }
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
        LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
        String trueValue = localeManager.getLocaleMessage("command-chat-info-true");
        String falseValue = localeManager.getLocaleMessage("command-chat-info-false");

        return super.getInfoPlaceholders()
                .add("type", this.channelType.toString().toLowerCase())
                .add("use-allies", this.useAllies ? trueValue : falseValue);
    }

    public TownyChannelType getChannelType() {
        return this.channelType;
    }

    public enum TownyChannelType {

        TOWN,
        NATION

    }

}
