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
        World world = player.getWorld();

        PlayerData playerData = this.playerDataManager.getPlayerDataSynchronous(player.getUniqueId());
        if (playerData.getCurrentChannel() == null) {
            boolean foundChannel = false;

            // Place the player in the correct channel.
            /*for (Channel channel : this.channelManager.getChannels().values()) {
                if (channel.isAutoJoin() && (channel.getWorld() != null && channel.getWorld().equalsIgnoreCase(world.getName()))) {
                    playerData.setCurrentChannel(channel);
                    channel.add(playerData.getUUID());
                    foundChannel = true;
                    break;
                }
            }*/

            // If no channel was found, place them in the default channel.
            if (!foundChannel) {
                playerData.setCurrentChannel(this.channelManager.getDefaultChannel());
                this.channelManager.getDefaultChannel().onJoin(player);
            }

            playerData.save();
        } else {
            playerData.getCurrentChannel().onJoin(player);
        }

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

        /*for (ChatChannel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getCommand() != null) {
                String command = channel.getCommand();
                event.getCommands().remove(command + ":" + command);

                if (!event.getPlayer().hasPermission("rosechat.channel." + channel.getId())) event.getCommands().remove(command);
            }
        }*/
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        RoseChatAPI api = RoseChatAPI.getInstance();
        Player player = event.getPlayer();
        World world = player.getWorld();
        PlayerData playerData = api.getPlayerData(player.getUniqueId());

        /*for (ChatChannel channel : api.getChannels()) {
            if (channel.getWorld() == null) continue;

            // Remove the player from the channel when leaving the world.
            if (channel.getWorld().equals(event.getFrom().getName())) {
               // ChatChannel defaultChannel = api.getChannelManager().getDefaultChannel();
                playerData.getCurrentChannel().remove(player);
              //  playerData.setCurrentChannel(defaultChannel);
               // defaultChannel.add(playerData.getUUID());
                playerData.save();
            }

            if (channel.getWorld().equalsIgnoreCase(world.getName()) && channel.isAutoJoin()) {
                playerData.getCurrentChannel().remove(player);
                playerData.setCurrentChannel(channel);
                channel.add(player);
                playerData.save();
                return;
            }
        }*/
    }

}
