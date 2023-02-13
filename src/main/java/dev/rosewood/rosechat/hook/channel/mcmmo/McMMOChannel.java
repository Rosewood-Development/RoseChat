package dev.rosewood.rosechat.hook.channel.mcmmo;

import com.gmail.nossr50.api.PartyAPI;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class McMMOChannel extends RoseChatChannel {

    public McMMOChannel(ChannelProvider provider) {
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
            player.sendMessage("[Party] " + message);
        }
    }

    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        return new ArrayList<>(PartyAPI.getMembersMap(sender.asPlayer()).keySet());
    }

}
