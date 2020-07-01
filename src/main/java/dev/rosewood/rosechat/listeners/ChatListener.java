package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageWrapper;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private RoseChat plugin;

    public ChatListener() {
        plugin = RoseChat.getInstance();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission("rosechat.chat")) return;

        Player player = event.getPlayer();
        String oldMessage = event.getMessage();
        event.setCancelled(true);

        MessageWrapper messageWrapper = new MessageWrapper(player, oldMessage).parsePlaceholders("chat-format");

        // Don't cancel the message if it is null? Would use default format?
        if (messageWrapper.isEmpty()) return;

        if (messageWrapper.isBlocked()) {
            messageWrapper.getFilterType().getWarning().sendMessage(player);
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
