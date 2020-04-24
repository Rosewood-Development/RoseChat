package dev.rosewood.rosechat.channels;

import org.bukkit.entity.Player;

public class StaffChat extends ChatChannel {

    public StaffChat() {
        super(ChannelType.OTHER);
    }

    @Override
    public void removePlayer(Player player) {
        // Players can not be removed from this channel.
    }
}
