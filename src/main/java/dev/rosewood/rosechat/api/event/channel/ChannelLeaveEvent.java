package dev.rosewood.rosechat.api.event.channel;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.RosePlayer;

public class ChannelLeaveEvent extends ChannelEvent {

    private final RosePlayer player;

    /**
     * Called when a player leaves a group.
     * @param channel The {@link Channel} for the channel that was left.
     * @param player The {@link RosePlayer} for the person leaving the channel.
     */
    public ChannelLeaveEvent(Channel channel, RosePlayer player) {
        super(channel);
        this.player = player;
    }

    public RosePlayer getPlayer() {
        return this.player;
    }

}
