package dev.rosewood.rosechat.message;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosegarden.RosePlugin;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Optional;

public class ChatPreviewHandler {

    private final RosePlugin rosePlugin;
    private PacketListener packetListener;

    public ChatPreviewHandler(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    public void enable() {
        DataManager dataManager = this.rosePlugin.getManager(DataManager.class);
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        this.packetListener = new PacketAdapter(PacketAdapter.params().plugin(this.rosePlugin).clientSide().listenerPriority(ListenerPriority.MONITOR).types(PacketType.Login.Client.START, PacketType.Play.Client.CHAT_PREVIEW)) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.isPlayerTemporary() || event.isCancelled())
                    return;

                PacketType packetType = event.getPacketType();
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();

                // Disable message signing
                if (packetType == PacketType.Login.Client.START) {
                    packet.getModifier().write(1, Optional.empty());
                    return;
                }

                if (packetType != PacketType.Play.Client.CHAT_PREVIEW)
                    return;

                // Respond to chat preview requests
                int responseId = packet.getIntegers().read(0);
                String message = packet.getStrings().read(0);
                if (message.isEmpty())
                    return;

                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    PacketContainer chatPreviewPacket = protocolManager.createPacket(PacketType.Play.Server.CHAT_PREVIEW);
                    chatPreviewPacket.getIntegers().write(0, responseId);

                    dataManager.getPlayerData(player.getUniqueId(), data -> {
                        ChatChannel channel = data.getCurrentChannel();
                        RoseSender sender = new RoseSender(player);

                        MessageWrapper messageWrapper = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, message).filter().applyDefaultColor();
                        chatPreviewPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(messageWrapper.parse(channel.getFormat(), sender))));
                        protocolManager.sendServerPacket(player, chatPreviewPacket);
                    });
                });
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener);
    }

    public void disable() {
        if (this.packetListener != null)
            ProtocolLibrary.getProtocolManager().removePacketListener(this.packetListener);
    }

}
