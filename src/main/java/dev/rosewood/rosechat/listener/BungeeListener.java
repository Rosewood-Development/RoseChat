package dev.rosewood.rosechat.listener;

import com.google.common.collect.Iterables;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BungeeListener implements PluginMessageListener {

    private final RoseChat plugin;
    private final PlayerDataManager playerDataManager;

    public BungeeListener(RoseChat plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getManager(PlayerDataManager.class);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord")) return;

        ByteArrayInputStream bytes = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bytes);

        try {
            String commandInfo = in.readUTF();
            String[] commandInfoSplit = commandInfo.split(":");
            String namespace = commandInfoSplit[0];
            String command = commandInfoSplit.length > 1 ? commandInfoSplit[1] : null;
            String additional = commandInfoSplit.length > 2 ? commandInfoSplit[2] : null;

            if (command == null && commandInfo.equalsIgnoreCase("PlayerList")) {
                String server = in.readUTF();
                String[] players = in.readUTF().split(", ");
                this.playerDataManager.getBungeePlayers().putAll(server, Arrays.asList(players));
                return;
            }

            if (!namespace.equalsIgnoreCase("rosechat")) return;

            byte[] msgBytes = new byte[in.readShort()];
            in.readFully(msgBytes);
            DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));
            String received = msgIn.readUTF();

            if (command.equalsIgnoreCase("message_player")) {
                PlayerData playerData = this.playerDataManager.getPlayerData(player.getUniqueId());
                if (additional == null || !playerData.getIgnoringPlayers().contains(UUID.fromString(additional))) {
                    RoseSender roseSender = new RoseSender(commandInfoSplit[3], commandInfoSplit[4]);
                    List<String> permissions = new ArrayList<>(Arrays.asList(commandInfoSplit[5].split(",")));
                    roseSender.setIgnoredPermissions(permissions);
                    MessageUtils.sendPrivateMessage(roseSender, player.getName(), received);
                }
            } else if (command.equalsIgnoreCase("update_reply")) {
                PlayerData playerData = this.plugin.getManager(PlayerDataManager.class).getPlayerData(player.getUniqueId());
                if (playerData != null) {
                    playerData.setReplyTo(received);
                    playerData.save();
                }
            } else {
                String channelTo = commandInfoSplit[1];
                String sender = commandInfoSplit[2];
                String senderUUID = commandInfoSplit[3];
                String senderGroup = commandInfoSplit[4];
                String senderPermissions = commandInfoSplit[5];
                List<String> permissions = new ArrayList<>(Arrays.asList(senderPermissions.split(",")));
                ChatChannel chatChannel = this.plugin.getManager(ChannelManager.class).getChannel(channelTo);
                RoseSender roseSender = new RoseSender(UUID.fromString(senderUUID), sender, senderGroup);
                roseSender.setIgnoredPermissions(permissions);
                Bukkit.getServer().getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> chatChannel.sendJson(roseSender, received));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a BungeeCord message to the specified channel on the specified server.
     * The sending and receiving server may have different format. Therefore, the raw message is sent so the
     * receiving server can parse it correctly.
     * A sender should contain a list of RoseChat permissions, which get sent to the specified server.
     * This is because if the receiving server has never seen that player, their message will not be seen as intended.
     * Due to this, any custom Tokenizers should start their permission with 'rosechat.', to avoid sending every single permission over the network.
     *
     * @param sender The RoseSender who sent the message.
     * @param serverTo The server that should receive the message.
     * @param channelTo The channel that should receive the message.
     * @param rawMessage The unformatted message that is being sent.
     */
    public static void sendChannelMessage(RoseSender sender, String serverTo, String channelTo, String rawMessage) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF("Forward");
            out.writeUTF(serverTo);

            StringBuilder stringBuilder = new StringBuilder();
            for (String permission : sender.getPermissions()) {
                if (stringBuilder.length() != 0) stringBuilder.append(",");
                stringBuilder.append(permission);
            }

            if (sender.isPlayer() && sender.asPlayer().isOp() || sender.hasPermission("*")) stringBuilder.append("rosechat.*");

            out.writeUTF("rosechat:" + channelTo + ":" + sender.getName() + ":" + sender.getUUID() + ":" + sender.getGroup() + ":" + stringBuilder.toString());

            sendPluginMessage(outputStream, out, rawMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendDirectMessage(RoseSender sender, String playerTo, String rawMessage) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(playerTo);

            StringBuilder stringBuilder = new StringBuilder();
            for (String permission : sender.getPermissions()) {
                if (stringBuilder.length() != 0) stringBuilder.append(",");
                stringBuilder.append(permission);
            }

            if (sender.isPlayer() && sender.asPlayer().isOp() || sender.hasPermission("*")) stringBuilder.append("rosechat.*");

            out.writeUTF("rosechat:message_player:" + sender.getUUID() + ":" + sender.getName() + ":" + sender.getGroup() + ":" + stringBuilder.toString());

            sendPluginMessage(outputStream, out, rawMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getPlayers(String server) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF("PlayerList");
            out.writeUTF(server);

            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) player.sendPluginMessage(RoseChat.getInstance(), "BungeeCord", outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateReply(String sender, String receiver) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(receiver);
            out.writeUTF("rosechat:update_reply");

            sendPluginMessage(outputStream, out, sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendPluginMessage(ByteArrayOutputStream outputStream, DataOutputStream out, String message) {
        try {
            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            msgOut.writeUTF(message);

            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) player.sendPluginMessage(RoseChat.getInstance(), "BungeeCord", outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
