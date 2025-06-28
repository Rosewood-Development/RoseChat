package dev.rosewood.rosechat.chat.log;

import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class ChannelMessageLog {

    protected final List<ChannelMessageOptions> messages;

    public ChannelMessageLog() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(ChannelMessageOptions options) {
        this.messages.add(options);
    }

    public ChannelMessageOptions getAndRemoveNextMessage() {
        ChannelMessageOptions options = this.getNextMessage();
        this.messages.removeIf(o -> o.equals(options));

        return options;
    }

    public ChannelMessageOptions getNextMessage() {
        return this.messages.isEmpty() ? null : this.messages.get(0);
    }

    public ChannelMessageOptions getAndRemoveNextMessage(Player player) {
        ChannelMessageOptions options = this.getNextMessage(player);
        this.messages.removeIf(o -> o.equals(options));

        return options;
    }

    public ChannelMessageOptions getNextMessage(Player player) {
        for (ChannelMessageOptions options : this.messages) {
            if (options.sender() == null || options.sender().getUUID() == null)
                continue;

            if (options.sender().getUUID().equals(player.getUniqueId()))
                return options;
        }

        return null;
    }

    public List<ChannelMessageOptions> getMessages() {
        return this.messages;
    }

}
