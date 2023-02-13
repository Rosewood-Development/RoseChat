package dev.rosewood.rosechat.hook.channel.kingdomsx;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KingdomsXChannel extends RoseChatChannel {

    public KingdomsXChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
    }

    @Override
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Kingdom] " + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        Kingdom kingdom = KingdomPlayer.getKingdomPlayer(sender.getUUID()).getKingdom();
        if (kingdom == null) return new ArrayList<>();
        return new ArrayList<>(kingdom.getMembers());
    }

}
