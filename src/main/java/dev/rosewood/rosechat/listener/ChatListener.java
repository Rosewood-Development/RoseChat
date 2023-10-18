package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        String message = event.getMessage();

        RoseChatAPI api = RoseChatAPI.getInstance();
        // Check if the message is using a shout command and send the message if they are.
        for (Channel channel : api.getChannels()) {
            if (channel.getShoutCommands().isEmpty()) continue;
            for (String command : channel.getShoutCommands()) {
                if (message.startsWith(command)) {
                    api.sendToChannel(event.getPlayer(), message.substring(command.length()).trim(), channel, true);
                    return;
                }
            }
        }

        RosePlayer sender = new RosePlayer(player);
        PlayerData data = sender.getPlayerData();

        Channel channel = data.getCurrentChannel();
        api.sendToChannel(player, message, channel, true);
    }

}
