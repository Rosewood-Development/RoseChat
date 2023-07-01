package dev.rosewood.rosechat.api.event.channel;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.RosePlayer;

public class ChannelJoinEvent extends ChannelEvent {

    private final RosePlayer player;

    /**
     * Called when a player joins a group.
     * @param channel The {@link Channel} for the channel that was joined.
     * @param player The {@link RosePlayer} for the person joining the channel.
     */
    public ChannelJoinEvent(Channel channel, RosePlayer player) {
        super(channel);
        this.player = player;
    }

    public RosePlayer getPlayer() {
        return this.player;
    }

}
