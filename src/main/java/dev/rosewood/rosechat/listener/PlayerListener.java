package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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
        RosePlayer player = new RosePlayer(event.getPlayer());

        // Ensure group chats are loaded first.
        RoseChatAPI.getInstance().getGroupManager().loadMemberGroupChats(player.getUUID(), (gcs) -> {
            PlayerData playerData = this.playerDataManager.getPlayerDataSynchronous(player.getUUID());

            // Set the display name when the player logs in
            if (playerData.getNickname() != null)
                player.updateDisplayName();
            else
                playerData.setDisplayName(event.getPlayer().getDisplayName());

            // If the current channel is not a group channel, put the player in the right channel.
            if (!playerData.isCurrentChannelGroupChannel() || playerData.getCurrentChannel() == null) {
                playerData.setIsInGroupChannel(false);

                // Place the player in the correct channel.
                for (Channel channel : this.channelManager.getChannels().values()) {
                    if (channel.onLogin(player)) {
                        player.switchChannel(channel);
                        break;
                    }
                }

                // If no channel was found, force put player in the default channel.
                if (playerData.getCurrentChannel() == null) {
                    Channel defaultChannel = this.channelManager.getDefaultChannel();
                    player.switchChannel(defaultChannel);
                } else {
                    Channel channel = playerData.getCurrentChannel();
                    player.switchChannel(channel);
                }
            }

            player.validateChatColor();
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RoseChat.getInstance(), () -> {
            RosePlayer player = new RosePlayer(event.getPlayer());
            this.playerDataManager.getPlayerData(player.getUUID()).save();
            this.playerDataManager.getPlayerData(player.getUUID()).getCurrentChannel().onLeave(player);
            this.playerDataManager.unloadPlayerData(player.getUUID());
        }, 5L);
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
        RosePlayer player = new RosePlayer(event.getPlayer());
        api.getPlayerDataManager().getPlayerData(player.getUUID(), (playerData -> {
            World world = player.asPlayer().getWorld();
            Channel currentChannel = playerData.getCurrentChannel();

            // Check if the player can leave their current channel first.
            boolean leftWorld = currentChannel.onWorldLeave(player, event.getFrom(), world);

            // Find the appropriate channel before removing the player.
            // Loop through the channels to find if the player should join one.
            boolean joinedWorld = false;
            for (Channel channel : api.getChannels()) {
                // If the player can join a channel, join.
                if (channel.onWorldJoin(player, event.getFrom(), event.getPlayer().getWorld())) {
                    if (player.switchChannel(channel)) {
                        player.sendLocaleMessage("command-channel-joined", StringPlaceholders.of("id", channel.getId()));

                        joinedWorld = true;
                    }

                    break;
                }
            }

            // If the player left a world channel and did not find an appropriate channel
            if (leftWorld && !joinedWorld) {
                Channel defaultChannel = api.getChannelManager().getDefaultChannel();
                player.switchChannel(defaultChannel);
            }

            playerData.save();
        }));
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String input = event.getMessage();
        RosePlayer player = new RosePlayer(event.getPlayer());

        for (Channel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getOverrideCommands().isEmpty())
                continue;

            for (String command : channel.getOverrideCommands()) {
                if (input.equalsIgnoreCase("/" + command) || input.toLowerCase().startsWith("/" + command.toLowerCase() + " ")) {
                    if (!channel.canJoinByCommand(player)) {
                        player.sendLocaleMessage("command-channel-not-joinable");
                        return;
                    }

                    if (channel.getId().equals(player.getPlayerData().getCurrentChannel().getId()))
                        channel = RoseChatAPI.getInstance().getDefaultChannel();

                    // If the message was the same, join the channel.
                    if (input.equalsIgnoreCase("/" + command)) {
                        boolean success = player.switchChannel(channel, channel instanceof GroupChannel);
                        if (!success)
                            return;

                        player.switchChannel(channel);
                        player.sendLocaleMessage("command-channel-joined",
                                StringPlaceholders.of("id", channel.getId()));
                    } else {
                        String message = input.substring(("/" + command + " ").length());
                        player.quickChat(channel, message);
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

}
