package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final RoseChat plugin;

    public PlayerListener(RoseChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        ChannelManager channelManager = this.plugin.getManager(ChannelManager.class);
        PlayerDataManager playerDataManager = this.plugin.getManager(PlayerDataManager.class);

        RosePlayer player = new RosePlayer(event.getPlayer());

        // Ensure group chats are loaded first.
        RoseChatAPI.getInstance().getGroupManager().loadMemberGroupChats(player.getUUID(), (gcs) -> {
            PlayerData playerData = playerDataManager.getPlayerDataSynchronous(player.getUUID());

            // Set the display name when the player logs in
            if (playerData.getNickname() != null)
                player.updateDisplayName();
            else
                playerData.setDisplayName(event.getPlayer().getDisplayName());

            // If the current channel is not a group channel, put the player in the right channel.
            if (!playerData.isCurrentChannelGroupChannel() || playerData.getCurrentChannel() == null) {
                playerData.setIsInGroupChannel(false);

                // Place the player in the correct channel.
                for (Channel channel : channelManager.getChannels().values()) {
                    if (channel.onLogin(player)) {
                        player.switchChannel(channel);
                        break;
                    }
                }

                // If no channel was found, force put player in the default channel.
                if (playerData.getCurrentChannel() == null) {
                    Channel defaultChannel = channelManager.getDefaultChannel();
                    player.switchChannel(defaultChannel);
                } else {
                    Channel channel = playerData.getCurrentChannel();
                    player.switchChannel(channel);
                }
            }

            player.validateChatColor();
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (NMSUtil.getVersionNumber() < 19 || !Settings.ALLOW_CHAT_SUGGESTIONS.get())
            return;

        RosePlayer player = new RosePlayer(event.getPlayer());
        player.validateChatCompletion();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RoseChat.getInstance(), () -> {
            if (!event.getPlayer().isOnline()) {
                PlayerDataManager playerDataManager = this.plugin.getManager(PlayerDataManager.class);
                RosePlayer player = new RosePlayer(event.getPlayer());
                playerDataManager.getPlayerData(player.getUUID()).save();
                playerDataManager.getPlayerData(player.getUUID()).getCurrentChannel().onLeave(player);
                playerDataManager.unloadPlayerData(player.getUUID());
            }
        }, 20L * 60L);
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        // Don't send the delete message command, as it shouldn't be used by players.
        event.getCommands().remove("delmsg");
        event.getCommands().remove("rosechat:delmsg");

        for (Channel channel : RoseChatAPI.getInstance().getChannels()) {
            if (channel.getSettings().getCommands().isEmpty())
                continue;

            for (String command : channel.getSettings().getCommands()) {
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
            if (channel.getSettings().getOverrideCommands().isEmpty())
                continue;

            for (String command : channel.getSettings().getOverrideCommands()) {
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

                        String joinMessage = channel.getSettings().getFormats().get("join-message");
                        if (joinMessage != null)
                            player.send(RoseChatAPI.getInstance().parse(player, player, joinMessage));
                        else
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
