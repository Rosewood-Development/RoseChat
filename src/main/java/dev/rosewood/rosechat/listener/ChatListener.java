package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.chat.PlayerData;
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
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        // Force the event to run async.
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Player player = event.getPlayer();
            PlayerData data = this.api.getPlayerData(player.getUniqueId());
            ChatChannel channel = data.getCurrentChannel();

            // Don't send the message if the player doesn't have permission.
            if (!player.hasPermission("rosechat.chat")) {
                this.api.getLocaleManager().sendMessage(player, "no-permission");
                return;
            }

            // Don't send the message if the channel is muted.
            if (channel.isMuted() && !player.hasPermission("rosechat.bypass.mute")) {
                this.api.getLocaleManager().sendMessage(player, "channel-muted");
                return;
            }

            // Don't send the message if the player is muted.
            if (data.isMuted() && !player.hasPermission("rosechat.bypass.mute")) {
                this.api.getLocaleManager().sendMessage(player, "");
                return;
            }

            // edit this
            MessageWrapper message = new MessageWrapper(player, data.getColor() + event.getMessage(), data.getCurrentChannel());

            if (message.canBeSent() && !message.isEmpty()) {
                Bukkit.spigot().broadcast(message.build());
               // MessageUtils.sendStandardMessage();
            } else {
                if (message.getFilterType() != null) message.getFilterType().sendWarning(player);
            }

            // Outputs the message to the console, with information about the hover events.
            /*if (ConfigurationManager.Setting.OUTPUT_HOVER_EVENTS.getBoolean()) {
                String hoverOutput = message.getHoverAsString();
                if (hoverOutput != null && !hoverOutput.isEmpty())
                    Bukkit.getConsoleSender().sendMessage(hoverOutput);
            }*/
        });
    }
}