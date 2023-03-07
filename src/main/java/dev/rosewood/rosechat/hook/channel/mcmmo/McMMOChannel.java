package dev.rosewood.rosechat.hook.channel.mcmmo;

import com.gmail.nossr50.api.PartyAPI;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class McMMOChannel extends RoseChatChannel {

    public McMMOChannel(ChannelProvider provider) {
        super(provider);
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

        LinkedHashMap<UUID, String> members = PartyAPI.getMembersMap(sender.asPlayer());
        if (members == null) return recipients;
        for (UUID member : members.keySet()) {
            Player player = Bukkit.getPlayer(member);
            if (player == null) continue;
            recipients.add(player);
        }

        return recipients;
    }

}
