package dev.rosewood.rosechat.hook.channel.mcmmo;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class McMMOChannel extends RoseChatChannel {

    public McMMOChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return super.getVisibleAnywhereRecipients(sender, world);
    }

   /*@Override
    public List<UUID> getMembers(RosePlayer sender) {
        return new ArrayList<>(PartyAPI.getMembersMap(sender.asPlayer()).keySet());
    }*/

}
