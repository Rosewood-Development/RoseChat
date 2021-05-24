package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DataManager dataManager;
    private final ChannelManager channelManager;

    public PlayerListener(RoseChat plugin) {
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

                // Place the player in the correct channel.
                for (ChatChannel channel : this.channelManager.getChannels().values()) {
                    if (channel.isAutoJoin() && (channel.getWorld() != null && channel.getWorld().equalsIgnoreCase(world.getName()))) {
                        playerData.setCurrentChannel(channel);
                        foundChannel = true;
                        break;
                    }
                }

                // If no channel was found, place them in the default channel.
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
        this.dataManager.getPlayerData(player.getUniqueId()).getCurrentChannel().remove(player);
        this.dataManager.unloadPlayerData(player.getUniqueId());
    }
}
