package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.MessageUtils;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private DataManager dataManager;

    public ChatListener(RoseChat plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        dataManager = plugin.getManager(DataManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!player.hasPermission("rosechat.chat")) return;

        String message = event.getMessage();
        ChatChannel channel = this.dataManager.getPlayerData(player.getUniqueId()).getCurrentChannel();

        MessageWrapper messageWrapper = new MessageWrapper(player, message)
                .checkAll()
                .filterAll()
                .withReplacements()
                .withTags()
                .inChannel(channel)
                .parsePlaceholders(channel.getFormatId(), null);

        MessageUtils.sendStandardMessage(player, messageWrapper, channel);
    }
}
