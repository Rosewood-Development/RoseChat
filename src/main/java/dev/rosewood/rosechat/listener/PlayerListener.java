package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
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
        Player player = event.getPlayer();
        RosePlayer rosePlayer = new RosePlayer(player);

        // Ensure group chats are loaded first.
        RoseChatAPI.getInstance().getGroupManager().loadMemberGroupChats(player.getUniqueId(), (gcs) -> {
            PlayerData playerData = this.playerDataManager.getPlayerDataSynchronous(player.getUniqueId());

            // If the current channel is not a group channel, put the player in the right channel.
            if (!playerData.isCurrentChannelGroupChannel() || playerData.getCurrentChannel() == null) {
                playerData.setIsInGroupChannel(false);

                // Place the player in the correct channel.
                for (Channel channel : this.channelManager.getChannels().values()) {
                    if (channel.onLogin(player)) {
                        rosePlayer.switchChannel(channel);
                        break;
                    }
                }

                // If no channel was found, force put player in the default channel.
                if (playerData.getCurrentChannel() == null) {
                    Channel defaultChannel = this.channelManager.getDefaultChannel();
                    rosePlayer.switchChannel(defaultChannel);
                } else {
                    Channel channel = playerData.getCurrentChannel();
                    rosePlayer.switchChannel(channel);
                }
            }

            // Set the display name when the player logs in
            if (playerData.getNickname() != null) {
                RoseMessage message = RoseMessage.forLocation(rosePlayer, PermissionArea.NICKNAME);
                MessageTokenizerResults<BaseComponent[]> components = message.parse(rosePlayer, playerData.getNickname());
                player.setDisplayName(TextComponent.toLegacyText(components.content()));

                if (RoseChat.getInstance().getNicknameProvider() != null) {
                    RoseChat.getInstance().getNicknameProvider().setNickname(player, player.getDisplayName());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RoseChat.getInstance(), () -> {
            Player player = event.getPlayer();
            this.playerDataManager.getPlayerData(player.getUniqueId()).save();
            this.playerDataManager.getPlayerData(player.getUniqueId()).getCurrentChannel().onLeave(player);
            this.playerDataManager.unloadPlayerData(player.getUniqueId());
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
        Player player = event.getPlayer();
        RosePlayer rosePlayer = new RosePlayer(player);
        api.getPlayerDataManager().getPlayerData(player.getUniqueId(), (playerData -> {
            World world = player.getWorld();
            Channel currentChannel = playerData.getCurrentChannel();

            // Check if the player can leave their current channel first.
            boolean leftWorld = currentChannel.onWorldLeave(player, event.getFrom(), world);

            // Find the appropriate channel before removing the player.
            // Loop through the channels to find if the player should join one.
            boolean joinedWorld = false;
            for (Channel channel : api.getChannels()) {
                // If the player can join a channel, join.
                if (channel.onWorldJoin(player, event.getFrom(), event.getPlayer().getWorld())) {
                    if (rosePlayer.switchChannel(channel)) {
                        api.getLocaleManager().sendMessage(player, "command-channel-joined", StringPlaceholders.of("id", channel.getId()));

                        joinedWorld = true;
                    }

                    break;
                }
            }

            // If the player left a world channel and did not find an appropriate channel
            if (leftWorld && !joinedWorld) {
                Channel defaultChannel = api.getChannelManager().getDefaultChannel();
                rosePlayer.switchChannel(defaultChannel);
            }

            playerData.save();
        }));
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
                        /*if (!ChannelCommand.processChannelSwitch(event.getPlayer(), channel.getId())) {
                            RoseChatAPI.getInstance().getLocaleManager()
                                    .sendComponentMessage(event.getPlayer(), "command-channel-custom-usage", StringPlaceholders.of("channel", channel.getId()));
                        }*/
                        //TODO this
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
