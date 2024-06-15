package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.channel.ChannelChangeEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final RoseChatAPI api;

    public ChatListener() {
        this.api = RoseChatAPI.getInstance();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        RosePlayer player = new RosePlayer(event.getPlayer());
        PlayerData data = player.getPlayerData();

        String message = event.getMessage();

        player.validateMuteExpiry();
        player.validateChatColor();

        // Don't send the message if the player doesn't have permission.
        if (!player.hasPermission("rosechat.chat")) {
            player.sendLocaleMessage("no-permission");
            return;
        }

        if (MessageUtils.isMessageEmpty(message)) {
            player.sendLocaleMessage("message-blank");
            return;
        }

        // Check if the player is muted.
        if (data.isMuted() && !player.hasPermission("rosechat.mute.bypass")) {
            player.sendLocaleMessage("command-mute-cannot-send");
            return;
        }

        // Check if the message is using a shout command and send the message if they are.
        for (Channel channel : this.api.getChannels()) {
            if (channel.getShoutCommands().isEmpty())
                continue;

            for (String command : channel.getShoutCommands()) {
                if (!message.startsWith(command))
                    continue;

                if (channel.isMuted() && !player.hasPermission("rosechat.mute.bypass")) {
                    player.sendLocaleMessage("channel-muted");
                    return;
                }

                String format = channel.getShoutFormat() == null ?
                        channel.getFormat() : channel.getShoutFormat();

                channel.send(player, message.substring(command.length()).trim(), format);

                player.updateDisplayName();
                return;
            }
        }

        // Get the channel that the message should be sent to.
        Channel channel = data.getActiveChannel();
        if (channel == null)
            channel = data.getCurrentChannel();

        // If the player is somehow not in a channel, find the appropriate channel to put them in.
        if (channel == null) {
            channel = player.findChannel();
            player.switchChannel(channel);
        }

        if (channel.isMuted() && !player.hasPermission("rosechat.mute.bypass")) {
            player.sendLocaleMessage("channel-muted");
            return;
        }

        channel.send(player, message);
        player.updateDisplayName();
    }

}
