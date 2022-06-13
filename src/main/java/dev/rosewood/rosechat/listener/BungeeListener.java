package dev.rosewood.rosechat.listener;

import com.google.common.collect.Iterables;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DataManager;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class BungeeListener implements PluginMessageListener {

    private final RoseChat plugin;
    private final DataManager dataManager;

    public BungeeListener(RoseChat plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
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
                this.dataManager.getBungeePlayers().put(server, Arrays.asList(players));
                return;
            }

            byte[] msgBytes = new byte[in.readShort()];
            in.readFully(msgBytes);
            DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));
            String received = msgIn.readUTF();

            if (!namespace.equalsIgnoreCase("rosechat")) return;
            if (command.equalsIgnoreCase("message_player")) {
                PlayerData playerData = this.dataManager.getPlayerData(player.getUniqueId());
                if (additional == null || !playerData.getIgnoringPlayers().contains(UUID.fromString(additional))) {
                    player.spigot().sendMessage(ComponentSerializer.parse(received));
                }
            } else if (command.equalsIgnoreCase("update_reply")) {
                PlayerData playerData = this.plugin.getManager(DataManager.class).getPlayerData(player.getUniqueId());
                if (playerData != null) {
                    playerData.setReplyTo(received);
                    playerData.save();
                }
            } else {
                ChatChannel chatChannel = this.plugin.getManager(ChannelManager.class).getChannel(command);
                // TODO: This
                //chatChannel.sendJson(received, null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendChannelMessage(String channel, String server, String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF("Forward");
            out.writeUTF(server);
            out.writeUTF("rosechat:" + channel);

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

    public static void sendDirectMessage(UUID sender, String playerName, String message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(outputStream);

        try {
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(playerName);
            out.writeUTF("rosechat:message_player:" + sender.toString());

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

            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            msgOut.writeUTF(sender);

            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) player.sendPluginMessage(RoseChat.getInstance(), "BungeeCord", outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
