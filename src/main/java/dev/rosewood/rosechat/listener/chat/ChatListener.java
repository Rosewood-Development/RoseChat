package dev.rosewood.rosechat.listener.chat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface ChatListener extends Listener {

    default void handleChat(Player player, String chat) {
        RoseChatAPI api = RoseChatAPI.getInstance();
        // Check if the message is using a shout command and send the message if they are.
        for (Channel channel : api.getChannels()) {
            if (channel.getShoutCommands().isEmpty()) continue;
            for (String command : channel.getShoutCommands()) {
                if (chat.startsWith(command)) {
                    api.sendToChannel(player, chat.substring(command.length()).trim(), channel, true);
                    return;
                }
            }
        }

        RosePlayer sender = new RosePlayer(player);
        PlayerData data = sender.getPlayerData();

        Channel channel = data.getCurrentChannel();
        api.sendToChannel(player, chat, channel, true);
    }

    static ChatListener create() {
        if (NMSUtil.isPaper()) {
            return new PaperChatListener();
        } else {
            return new BukkitChatListener();
        }
    }

}
