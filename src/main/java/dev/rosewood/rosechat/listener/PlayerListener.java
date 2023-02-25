package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ChannelManager channelManager;
    private final PlayerDataManager playerDataManager;

    public PlayerListener(RoseChat plugin) {
        this.channelManager = plugin.getManager(ChannelManager.class);
        this.playerDataManager = plugin.getManager(PlayerDataManager.class);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = this.playerDataManager.getPlayerDataSynchronous(player.getUniqueId());

        // Place the player in the correct channel.
        for (Channel channel : this.channelManager.getChannels().values()) {
            if (channel.onLogin(player)) {
                playerData.setCurrentChannel(channel);
                break;
            }
        }

        // If no channel was found, force put player in the default channel.
        if (playerData.getCurrentChannel() == null) {
            Channel defaultChannel = this.channelManager.getDefaultChannel();
            defaultChannel.onJoin(player);
            playerData.setCurrentChannel(defaultChannel);
        } else {
            Channel channel = playerData.getCurrentChannel();
            channel.onJoin(player);
        }

        playerData.save();

        if (playerData.getNickname() != null) NicknameCommand.setDisplayName(player, playerData.getNickname());

        RoseChatAPI.getInstance().getGroupManager().loadMemberGroupChats(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.playerDataManager.getPlayerData(player.getUniqueId()).save();
        this.playerDataManager.getPlayerData(player.getUniqueId()).getCurrentChannel().onLeave(player);
        this.playerDataManager.unloadPlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        // Don't send the delete message command, as it shouldn't be used by players.
        event.getCommands().remove("delmsg");
        event.getCommands().remove("rosechat:delmsg");

        for (Channel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getCommands().isEmpty()) continue;
            for (String command : channel.getCommands()) {
                event.getCommands().remove(command + ":" + command);
                if (!event.getPlayer().hasPermission("rosechat.channel." + channel.getId()))
                    event.getCommands().remove(command);
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        RoseChatAPI api = RoseChatAPI.getInstance();
        Player player = event.getPlayer();
        PlayerData playerData = api.getPlayerData(player.getUniqueId());
        World world = player.getWorld();
        Channel currentChannel = playerData.getCurrentChannel();

        // Check if the player can leave their current channel first.
        if (currentChannel.onWorldLeave(player, event.getFrom(), world)) {
            // Leave the channel
            currentChannel.onLeave(player);
            // Temporarily set the current channel to null
            playerData.setCurrentChannel(null);
        }

        // Loop through the channels to find if the player should join one.
        boolean foundChannel = false;
        for (Channel channel : api.getChannels()) {
            // If the player can join a channel, join.
            if (channel.onWorldJoin(player, event.getFrom(), event.getPlayer().getWorld())) {
                channel.onJoin(player);
                playerData.setCurrentChannel(channel);
                foundChannel = true;
                break;
            }

        }

        // If no suitable channel was found, put the player in the default channel.
        if (!foundChannel) {
            // Force join the default channel as there is no other option
            Channel defaultChannel = api.getChannelManager().getDefaultChannel();
            defaultChannel.onJoin(player);
            playerData.setCurrentChannel(defaultChannel);
        }

        playerData.save();
    }

}
