package dev.rosewood.rosechat.hook.channel.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TownyChannel extends RoseChatChannel {

    private TownyChannelType channelType;
    private boolean useAllies;

    public TownyChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        if (config.contains("channel-type")) this.channelType = TownyChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (config.contains("use-allies")) this.useAllies = config.getBoolean("use-allies");
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        if (this.channelType == TownyChannelType.TOWN) {
            Town town = TownyAPI.getInstance().getTown(sender.asPlayer());
            if (town == null) return recipients;

            for (Resident resident : town.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getUUID());
                if (player != null) recipients.add(player);
            }

            if (this.useAllies) {
                for (Town ally : town.getAllies()) {
                    for (Resident resident : ally.getResidents()) {
                        Player player = Bukkit.getPlayer(resident.getUUID());
                        if (player != null) recipients.add(player);
                    }
                }
            }
        } else {
            Nation nation = TownyAPI.getInstance().getNation(sender.asPlayer());
            if (nation == null) return recipients;

            for (Resident resident : nation.getResidents()) {
                Player player = Bukkit.getPlayer(resident.getUUID());
                if (player != null) recipients.add(player);
            }

            if (this.useAllies) {
                for (Nation ally : nation.getAllies()) {
                    for (Resident resident : ally.getResidents()) {
                        Player player = Bukkit.getPlayer(resident.getUUID());
                        if (player != null) recipients.add(player);
                    }
                }
            }
        }

        return recipients;
    }

    public TownyChannelType getChannelType() {
        return this.channelType;
    }

    public enum TownyChannelType {

        TOWN,
        NATION

    }

}
