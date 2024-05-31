package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.channel.ChannelChangeEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
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

        // Remove the chat colour if the player no longer has permission for it.
        RosePlayer sender = new RosePlayer(player);
        if (!MessageUtils.canColor(sender, sender.getPlayerData().getColor(), MessageLocation.CHATCOLOR.toString().toLowerCase()))
            sender.getPlayerData().setColor("");

        String message = event.getMessage();

        RoseChatAPI api = RoseChatAPI.getInstance();
        // Check if the message is using a shout command and send the message if they are.
        for (Channel channel : api.getChannels()) {
            if (channel.getShoutCommands().isEmpty()) continue;
            for (String command : channel.getShoutCommands()) {
                if (message.startsWith(command)) {
                    String format = channel.getShoutFormat() == null ? channel.getFormat() : channel.getShoutFormat();
                    api.sendToChannel(event.getPlayer(), message.substring(command.length()).trim(), channel, format, true);
                    return;
                }
            }
        }

        PlayerData data = sender.getPlayerData();

        // Get the channel that the message should be sent to.
        Channel channel = data.getActiveChannel();
        if (channel == null) {
            channel = data.getCurrentChannel();
        }

        // If the player is somehow not in a channel, find the appropriate channel to put them in.
        if (channel == null) {
            channel = Channel.findNextChannel(player);

            ChannelChangeEvent channelChangeEvent = new ChannelChangeEvent(null, channel, player);
            Bukkit.getPluginManager().callEvent(channelChangeEvent);
            // This event is not cancellable as the player has to be in a channel to send a message.

            sender.changeChannel(null, channel);
        }

        api.sendToChannel(player, message, channel, true);
    }

}
