package dev.rosewood.rosechat.data;

import dev.rosewood.rosechat.channels.ChatChannel;

import java.util.ArrayList;
import java.util.List;

public class ChannelManager {

    private List<ChatChannel> channels;

    public ChannelManager() {
        channels = new ArrayList<>();
    }

    public void addChannel(ChatChannel channel) {
        channels.add(channel);
    }

    public void removeChannel(ChatChannel channel) {
        channels.remove(channel);
    }

    public List<ChatChannel> getChannels() {
        return channels;
    }
}
