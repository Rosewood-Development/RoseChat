package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final RoseChatAPI api;

    public ChatListener() {
        this.api = RoseChatAPI.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        // Check if the message is using a shout command and send the message if they are.
        for (Channel channel : this.api.getChannels()) {
            if (channel.getShoutCommands().isEmpty()) continue;
            for (String command : channel.getShoutCommands()) {
                if (event.getMessage().startsWith(command)) {
                    this.api.sendToChannel(event.getPlayer(), event.getMessage().substring(command.length()).trim(), channel, true);
                    return;
                }
            }
        }

        RosePlayer sender = new RosePlayer(event.getPlayer());
        PlayerData data = sender.getPlayerData();

        Channel channel = data.getCurrentChannel();
        this.api.sendToChannel(event.getPlayer(), event.getMessage(), channel, true);
    }

}
