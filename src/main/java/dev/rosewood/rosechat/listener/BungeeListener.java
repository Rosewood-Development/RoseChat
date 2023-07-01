package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.BungeeManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BungeeListener implements PluginMessageListener {

    private final BungeeManager bungeeManager;

    public BungeeListener(RoseChat plugin) {
        this.bungeeManager = plugin.getManager(BungeeManager.class);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord")) return;

        ByteArrayInputStream bytes = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bytes);

        try {
            String command = in.readUTF();
            if (command.equals("PlayerList")) {
                this.bungeeManager.receivePlayers(in.readUTF(), in.readUTF().split(", "));
                return;
            }

            // Make sure RoseChat is sending the message.
            if (!command.startsWith("rosechat")) return;
            command = command.substring("rosechat:".length());

            byte[] msgBytes = new byte[in.readShort()];
            in.readFully(msgBytes);
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(msgBytes));

            switch (command) {
                case "channel_message": {
                    String rcChannel = data.readUTF();
                    String sender = data.readUTF();
                    String uuidStr = data.readUTF();
                    UUID senderUUID = uuidStr.equalsIgnoreCase("null") ? null : UUID.fromString(uuidStr);
                    String group = data.readUTF();
                    List<String> permissions = Arrays.asList(data.readUTF().split(","));
                    String messageIdStr = data.readUTF();
                    UUID messageId = messageIdStr.equalsIgnoreCase("null") ? null : UUID.fromString(messageIdStr);
                    boolean isJson = data.readBoolean();
                    String rcMessage = data.readUTF();
                    this.bungeeManager.receiveChannelMessage(rcChannel, sender, senderUUID, group, permissions, messageId, isJson, rcMessage);
                    return;
                }
                case "direct_message": {
                    String sender = data.readUTF();
                    UUID senderUUID = UUID.fromString(data.readUTF());
                    String group = data.readUTF();
                    List<String> permissions = Arrays.asList(data.readUTF().split(","));
                    String rcMessage = data.readUTF();
                    this.bungeeManager.receiveDirectMessage(player, sender, senderUUID, group, permissions, rcMessage);
                    break;
                }
                case "update_reply": {
                    String sender = data.readUTF();
                    this.bungeeManager.receiveUpdateReply(player, sender);
                    break;
                }
                case "check_plugin": {
                    String sender = data.readUTF();
                    String plugin = data.readUTF();
                    this.bungeeManager.receivePluginCheck(sender, plugin);
                    break;
                }
                case "confirm_plugin": {
                    boolean hasPlugin = data.readBoolean();
                    String sender = data.readUTF();
                    this.bungeeManager.receivePluginCheckConfirmation(sender, hasPlugin);
                    break;
                }
                case "delete_message": {
                    UUID messageId = UUID.fromString(data.readUTF());
                    this.bungeeManager.receiveMessageDeletion(messageId);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
