package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final RoseChat plugin;
    private final RoseChatAPI api;

    public ChatListener(RoseChat plugin) {
        this.plugin = plugin;
        this.api = RoseChatAPI.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        RosePlayer sender = new RosePlayer(event.getPlayer());
        PlayerData data = sender.getPlayerData();

        // Don't send the message if the player doesn't have permission.
        if (data == null || !sender.hasPermission("rosechat.chat")) {
            this.api.getLocaleManager().sendComponentMessage(event.getPlayer(), "no-permission");
            return;
        }

        // Check the mute expiry.
        if (data.isMuteExpired()) {
            data.unmute();
            data.save();
        }

        // Check if the player is muted.
        if (data.isMuted() && !sender.hasPermission("rosechat.mute.bypass")) {
            sender.sendLocaleMessage("command-mute-cannot-send");
            return;
        }

        // Make the message isn't empty.
        if (MessageUtils.isMessageEmpty(event.getMessage())) {
            sender.sendLocaleMessage("message-blank");
            return;
        }

        // Force the event to run async.
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Channel channel = data.getCurrentChannel();

            // Check if the player has permission for this channel.
            if (!sender.hasPermission("rosechat.channel." + channel.getId())) {
                sender.sendLocaleMessage("no-permission");
                return;
            }

            // Send the message.
            channel.send(sender, event.getMessage());

            // Update the player's display name if the setting is enabled.
            if (Setting.UPDATE_DISPLAY_NAMES.getBoolean() && data.getNickname() != null && !sender.getDisplayName().equals(data.getNickname())) {
                RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                    RoseMessage message = new RoseMessage(sender, MessageLocation.NICKNAME, data.getNickname());
                    message.parse(sender, null);

                    if (data.getNickname() != null) NicknameCommand.setDisplayName(sender, message);
                });
            }
        });
    }

}
