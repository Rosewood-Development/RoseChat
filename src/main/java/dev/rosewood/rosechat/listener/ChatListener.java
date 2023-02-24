package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        // Force the event to run async.
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Player player = event.getPlayer();
            PlayerData data = this.api.getPlayerData(player.getUniqueId());
            //ChatChannel channel = data.getCurrentChannel();
            RosePlayer sender = new RosePlayer(player);

            Channel channel = this.plugin.getManager(ChannelManager.class).getChannels().get("global");
            channel.send(new RosePlayer(player), event.getMessage());
            //if (!channel.canSendMessage(sender, event.getMessage())) return;

            //MessageWrapper message = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, event.getMessage()).filter().applyDefaultColor();


            //MessageUtils.sendMessageWrapper(sender, channel, message);

            if (Setting.UPDATE_DISPLAY_NAMES.getBoolean() && !player.getDisplayName().equals(data.getNickname())) NicknameCommand.setDisplayName(player, data.getNickname());
        });
    }

}
