package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        World world = player.getWorld();

        for (ChatChannel channel : plugin.getManager(ChannelManager.class).getChannels().values()) {
            if (channel.isAutoJoin() && channel.getWorld().equalsIgnoreCase(world.getName())) {
                channel.add(player);
                return;
            }
        }

        plugin.getManager(ChannelManager.class).getDefaultChannel().add(player);
        plugin.getManager(DataManager.class).createPlayerMessageLog(player);
    }
}
