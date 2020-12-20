package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private RoseChat plugin;
    private DataManager dataManager;
    private ChannelManager channelManager;

    public PlayerListener(RoseChat plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
        this.channelManager = plugin.getManager(ChannelManager.class);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        this.dataManager.getPlayerData(player.getUniqueId(), (playerData) -> {
            if (playerData.getCurrentChannel() == null) {
                boolean foundChannel = false;
                for (ChatChannel channel : this.channelManager.getChannels().values()) {
                    if (channel.isAutoJoin() && channel.getWorld().equalsIgnoreCase(world.getName())) {
                        playerData.setCurrentChannel(channel);
                        foundChannel = true;
                        break;
                    }
                }

                if (!foundChannel) {
                    playerData.setCurrentChannel(this.channelManager.getDefaultChannel());
                }

                playerData.save();
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dataManager.getPlayerData(player.getUniqueId()).getCurrentChannel().remove(player);
        dataManager.unloadPlayerData(player.getUniqueId());
    }
}
