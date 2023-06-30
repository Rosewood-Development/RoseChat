package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.ChannelCommand;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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

        // Ensure group chats are loaded first.
        RoseChatAPI.getInstance().getGroupManager().loadMemberGroupChats(player.getUniqueId(), (gcs) -> {
            PlayerData playerData = this.playerDataManager.getPlayerDataSynchronous(player.getUniqueId());

            // If the current channel is not a group channel, put the player in the right channel.
            if (!playerData.isCurrentChannelGroupChannel() || playerData.getCurrentChannel() == null) {
                playerData.setIsInGroupChannel(false);

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
            }

            // Set the display name when the player logs in
            if (playerData.getNickname() != null) {
                RosePlayer rosePlayer = new RosePlayer(player);
                RoseMessage message = new RoseMessage(rosePlayer, MessageLocation.NICKNAME, playerData.getNickname());
                BaseComponent[] parsed = message.parse(rosePlayer, null);

                if (parsed == null) return;
                NicknameCommand.setDisplayName(rosePlayer, message);
            }
        });
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

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String input = event.getMessage();

        for (Channel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getOverrideCommands().isEmpty()) continue;
            for (String command : channel.getOverrideCommands()) {
                if (input.equalsIgnoreCase("/" + command) || input.toLowerCase().startsWith("/" + command.toLowerCase() + " ")) {

                    // If the message was the same, join the channel.
                    if (input.equalsIgnoreCase("/" + command)) {
                        if (!ChannelCommand.processChannelSwitch(event.getPlayer(), channel.getId())) {
                            RoseChatAPI.getInstance().getLocaleManager()
                                    .sendComponentMessage(event.getPlayer(), "command-channel-custom-usage", StringPlaceholders.single("channel", channel.getId()));
                        }
                    } else {
                        String message = input.substring(("/" + command + " ").length());
                        RoseChatAPI.getInstance().sendToChannel(event.getPlayer(), message, channel, true);
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

}
