package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import net.md_5.bungee.api.chat.BaseComponent;
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
            ChatChannel channel = data.getCurrentChannel();
            RoseSender sender = new RoseSender(player);

            if (!channel.canSendMessage(sender, data, event.getMessage())) return;

            MessageWrapper message = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, event.getMessage()).filter().applyDefaultColor();
            if (!message.canBeSent()) {
                if (message.getFilterType() != null) message.getFilterType().sendWarning(sender);
                return;
            }

            channel.send(message);
            //BaseComponent[] messageComponents = message.toComponents();
            //if (messageComponents != null) Bukkit.getConsoleSender().spigot().sendMessage(messageComponents);
        });
    }

}
