package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private RoseChat plugin;

    public ChatListener(RoseChat plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        if (!event.getPlayer().hasPermission("rosechat.chat")) return;

        Player player = event.getPlayer();
        String oldMessage = event.getMessage();

        // player data, get channel, get format
        MessageWrapper messageWrapper = new MessageWrapper(player, oldMessage)
                .checkAll()
                .filterCaps()
                .parsePlaceholders("channel-global", null, null);

        // Don't cancel the message if it is null? Would use default format?
        if (messageWrapper.isEmpty()) return;

        if (messageWrapper.isBlocked()) {
            if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().getWarning().sendMessage(player);
            return;
        }

        // Sends to all players and logs it to console.
        for (Player receiver : event.getRecipients()) messageWrapper.send(receiver);
        messageWrapper.send(Bukkit.getConsoleSender());

        if (plugin.getConfigFile().getString("tag-sound") == null) return;

        String soundStr = plugin.getConfigFile().getString("tag-sound");

        Sound sound;
        try {
            sound = Sound.valueOf(soundStr);
        } catch (Exception e) {
            return;
        }

        for (String playerStr : messageWrapper.getTaggedPlayerNames()) {
            Player tagged = Bukkit.getPlayer(playerStr);
            if (tagged != null) tagged.playSound(tagged.getLocation(), sound, 1, 1);
        }
    }
}
