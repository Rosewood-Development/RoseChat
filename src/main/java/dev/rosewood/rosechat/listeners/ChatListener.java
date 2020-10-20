package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    public ChatListener(RoseChat plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!player.hasPermission("rosechat.chat")) return;

        String oldMessage = event.getMessage();

        // player data, get channel, get format
        MessageWrapper messageWrapper = new MessageWrapper(player, oldMessage)
                .checkAll()
                .filterAll()
                .withReplacements()
                .withTags()
                // get player channel
                .parsePlaceholders("channel-global", null);

        // Don't cancel the message if it is null? Would use default format?
        if (messageWrapper.isEmpty()) return;

        if (messageWrapper.isBlocked()) {
            if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(player);
            return;
        }

        // Sends to all players and logs it to console.
        for (Player receiver : event.getRecipients()) messageWrapper.send(receiver);
        messageWrapper.send(Bukkit.getConsoleSender());

        if (messageWrapper.getTagSound() == null) return;

        for (String playerStr : messageWrapper.getTaggedPlayerNames()) {
            Player tagged = Bukkit.getPlayer(playerStr);
            if (tagged != null) tagged.playSound(tagged.getLocation(), messageWrapper.getTagSound(), 1, 1);
        }
    }
}
