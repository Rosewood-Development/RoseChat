package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private RoseChat plugin;

    public PlayerListener() {
        plugin = RoseChat.getInstance();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

}
