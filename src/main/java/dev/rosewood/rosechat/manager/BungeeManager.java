package dev.rosewood.rosechat.manager;

import com.google.common.collect.Iterables;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.DeleteMessageCommand;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BungeeManager extends Manager {

    private final List<String> checkPluginPlayers;

    public BungeeManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.checkPluginPlayers = new ArrayList<>();
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

    /**
     * Send a generic message to another server.
     * @param command The command to send.
     * @param to The server that the message should go to.
     * @param channel The channel to use.
     * @param msgBytes The bytes of the message.
     * @param msgOut The output stream for the message.
     */
    public void send(String command, String to, String channel, ByteArrayOutputStream msgBytes, DataOutputStream msgOut) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF(command);
            out.writeUTF(to);
            if (channel != null) out.writeUTF(channel);

            if (msgBytes != null && msgOut != null) {
                out.writeShort(msgBytes.toByteArray().length);
                out.write(msgBytes.toByteArray());
            }

            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) player.sendPluginMessage(RoseChat.getInstance(), "BungeeCord", outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the players on the given server.
     * @param server The server to use.
     */
    public void getPlayers(String server) {
        this.send("PlayerList", server, null, null, null);
    }

    public void receivePlayers(String server, String[] players) {
        RoseChatAPI.getInstance().getPlayerDataManager().getBungeePlayers().putAll(server, Arrays.asList(players));
    }

    //
    // Channel Messages
    //

    private String getPlayerPermissions(RosePlayer sender) {
        if ((sender.isPlayer() && sender.asPlayer().isOp())) return "*";

        StringBuilder stringBuilder = new StringBuilder();
        for (String permission : sender.getPermissions()) {
            if (stringBuilder.length() != 0) stringBuilder.append(",");
            stringBuilder.append(permission);
        }

        return stringBuilder.toString();
    }

    /**
     * Sends a BungeeCord message to the specified channel on the specified server.
     * The sending and receiving server may have different formats.
     * Each server parses the message independently.
     * This means that permissions and placeholders are sent to the receiving server.
     * Due to this, any custom Tokenizers should start their permission with 'rosechat.', to avoid sending every single permission over the network.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param server The server that should receive the message.
     * @param channel The channel that should receive the message.
     * @param messageId The {@link UUID} of the message that should be sent.
     * @param message The unformatted message that should be sent.
     */
    public void sendChannelMessage(RosePlayer sender, String server, String channel, UUID messageId, boolean isJson, String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF(channel);
            out.writeUTF(sender.getName());
            out.writeUTF(sender.getUUID().toString());
            out.writeUTF(sender.getGroup());
            out.writeUTF(this.getPlayerPermissions(sender));
            out.writeUTF(messageId.toString());
            out.writeBoolean(isJson);
            out.writeUTF(PlaceholderAPIHook.applyPlaceholders(sender.isPlayer() ? sender.asPlayer() : null, message));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.send("Forward", server, "rosechat:channel_message", outputStream, out);
    }

    /**
     * Called when the server receives a "channel_message" message.
     * @param senderStr The name of the sender.
     * @param senderUUID The {@link UUID} of the sender.
     * @param senderGroup The permission group of the sender.
     * @param permissions A list of RoseChat permissions that the sender has.
     * @param messageId The {@link UUID} of the message.
     * @param message The unformatted message received.
     */
    public void receiveChannelMessage(String channelStr, String senderStr, UUID senderUUID, String senderGroup, List<String> permissions,
                                      UUID messageId, boolean isJson, String message) {
        Channel channel = this.rosePlugin.getManager(ChannelManager.class).getChannel(channelStr);

        if (channel == null) return;
        RosePlayer sender = new RosePlayer(senderUUID, senderStr, senderGroup);
        sender.setIgnoredPermissions(permissions);

        // Must be done asynchronously for LuckPerms & Vault.
        RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
            if (isJson) {
                channel.sendJson(sender, message, messageId);
            } else {
                channel.send(sender, message, messageId);
            }
        });
    }

    //
    // Direct Messages
    //

    /**
     * Sends a BungeeCord message to a specific player.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param receiver The name of the player who received the message.
     * @param message The unformatted message to be sent.
     * @param callback A callback to check if the message was received.
     */
    public void sendDirectMessage(RosePlayer sender, String receiver, String message, Consumer<Boolean> callback) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF(sender.getName());
            out.writeUTF(sender.getUUID().toString());
            out.writeUTF(sender.getGroup());
            out.writeUTF(this.getPlayerPermissions(sender));
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sendPluginCheck(sender.getName(), receiver, "RoseChat", (hasPlugin) -> {
           if (hasPlugin)
               this.send("ForwardToPlayer", receiver, "rosechat:direct_message", outputStream, out);

           callback.accept(hasPlugin);
        });
    }

    /**
     * Called when a player receives a "direct_message" message.
     * @param player The {@link Player} who received the message.
     * @param senderStr The name of the player who sent the message.
     * @param senderUUID The {@link UUID} of the player who sent the message.
     * @param group The permission group of the player who sent the message.
     * @param permissions A list of RoseChat permissions that the sender has.
     * @param message The unformatted message being received.
     */
    public void receiveDirectMessage(Player player, String senderStr, UUID senderUUID, String group, List<String> permissions, String message) {
        PlayerData playerData = this.rosePlugin.getManager(PlayerDataManager.class).getPlayerData(player.getUniqueId());
        if (playerData.getIgnoringPlayers().contains(senderUUID)) return;

        RosePlayer sender = new RosePlayer(senderStr, group);
        sender.setIgnoredPermissions(permissions);
        MessageUtils.sendPrivateMessage(sender, player.getName(), message);
    }

    //
    // Plugin Check
    //

    /**
     * Checks if a plugin is on a player's server.
     * @param sender The name of the player receiving the message.
     * @param receiver The name of the player sending the message.
     * @param plugin The name of the plugin to check for.
     * @param callback True if the plugin exists on the server.
     */
    public void sendPluginCheck(String sender, String receiver, String plugin, Consumer<Boolean> callback) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF(sender);
            out.writeUTF(plugin);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.send("ForwardToPlayer", receiver, "rosechat:check_plugin", outputStream, out);

        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            int timeout = ConfigurationManager.Setting.BUNGEECORD_MESSAGE_TIMEOUT.getInt();
            long startTime = System.currentTimeMillis();
            while (startTime + timeout > System.currentTimeMillis()) {
                if (this.checkPluginPlayers.contains(sender)) {
                    this.checkPluginPlayers.remove(sender);
                    callback.accept(true);
                    return;
                }
            }

            callback.accept(false);
        });
    }

    /**
     * Called when the server receives a "check_plugin" message.
     * @param sender The name of the player who sent the original message.
     * @param plugin The name of the plugin that is being checked.
     */
    public void receivePluginCheck(String sender, String plugin) {
        this.sendPluginCheckConfirmation(sender, Bukkit.getServer().getPluginManager().getPlugin(plugin) != null);
    }

    /**
     * Sends a message to confirm that the plugin is or is not installed.
     * @param sender The name of the player who sent the original message.
     * @param hasPlugin True if the server has the plugin.
     */
    public void sendPluginCheckConfirmation(String sender, boolean hasPlugin) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeBoolean(hasPlugin);
            out.writeUTF(sender);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.send("ForwardToPlayer", sender, "rosechat:confirm_plugin", outputStream, out);
    }

    /**
     * Called when the server receives a "confirm_plugin" message.
     * @param player The name of the player who sent the original message.
     * @param hasPlugin True if the server has the plugin.
     */
    public void receivePluginCheckConfirmation(String player, boolean hasPlugin) {
        if (hasPlugin) this.checkPluginPlayers.add(player);
    }

    //
    // Message Replies
    //

    /**
     * Updates the player who should be replied to.
     * @param sender The name of the player that sent the message.
     * @param receiver The name of the player that received the message.
     */
    public void sendUpdateReply(String sender, String receiver) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF(sender);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.send("ForwardToPlayer", receiver, "rosechat:update_reply", outputStream, out);
    }

    /**
     * Called when the server receives the "update_reply" message.
     * @param player The player who should receive the message.
     * @param sender The name of the player who sent the message.
     */
    public void receiveUpdateReply(Player player, String sender) {
        PlayerData data = this.rosePlugin.getManager(PlayerDataManager.class).getPlayerData(player.getUniqueId());
        if (data != null) {
            data.setReplyTo(sender);
            data.save();
        }
    }

    //
    // Message Deletion
    //

    /**
     * Sends a message to delete a message.
     * @param server The server to delete the message on.
     * @param messageId The {@link UUID} of the message to delete.
     */
    public void sendMessageDeletion(String server, UUID messageId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF(messageId.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.send("Forward", server, "rosechat:delete_message", outputStream, out);
    }


    /**
     * Called when the server receives a "delete_message" message.
     * @param messageId The id of the message to delete.
     */
    public void receiveMessageDeletion(UUID messageId) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = this.rosePlugin.getManager(PlayerDataManager.class).getPlayerData(player.getUniqueId());
            for (DeletableMessage message : data.getMessageLog().getDeletableMessages()) {
                if (message.getUUID().equals(messageId)) {
                    DeleteMessageCommand.deleteMessageForPlayer(player, message);
                    break;
                }
            }
        }
    }

}
