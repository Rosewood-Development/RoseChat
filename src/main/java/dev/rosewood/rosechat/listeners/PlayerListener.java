package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

        ChannelManager channelManager = plugin.getManager(ChannelManager.class);
        DataManager dataManager = plugin.getManager(DataManager.class);

        for (ChatChannel channel : channelManager.getChannels().values()) {
            if (channel.isAutoJoin() && channel.getWorld().equalsIgnoreCase(world.getName())) {
                channel.add(player);
                return;
            }
        }

        channelManager.getDefaultChannel().add(player);
        dataManager.getPlayerData(player.getUniqueId(), (playerData) -> {
            Bukkit.broadcastMessage(ChatColor.RED + "Has Social Spy: " + playerData.hasSocialSpy());
        });

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DataManager dataManager = plugin.getManager(DataManager.class);
        dataManager.unloadPlayerData(player.getUniqueId());
    }
}
