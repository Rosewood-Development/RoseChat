package dev.rosewood.rosechat.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Bungee Messaging Test - WIP
public class BungeeListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("RoseChat")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        Bukkit.broadcastMessage(in.readUTF());
    }

    public static void sendChannelMessage(String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");

        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);

        try {
            msgOut.writeUTF(message);
        } catch (IOException exception) {

        }

        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());
    }
}
