package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private RoseChat plugin;

    public PlayerListener(RoseChat plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getChannelManager().getChannelById("global").add(event.getPlayer());
        // Add to default channel, or world channel if auto join is true
    }
}
