package dev.rosewood.rosechat.hook.channel.marriagemaster;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageMasterPlugin;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MarriageMasterChannel extends RoseChatChannel {

    private final MarriageMasterPlugin marriageMaster;

    public MarriageMasterChannel(ChannelProvider provider) {
        super(provider);
        this.marriageMaster = (MarriageMasterPlugin) Bukkit.getPluginManager().getPlugin("MarriageMaster");
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
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
        if (!this.marriageMaster.getPlayerData(player).isMarried())
            return false;

        return super.canJoinByCommand(player);
    }

}
