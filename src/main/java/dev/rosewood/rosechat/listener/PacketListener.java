package dev.rosewood.rosechat.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class PacketListener {

    public PacketListener(RoseChat plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            final RoseChatAPI api = RoseChatAPI.getInstance();

            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    Player player = event.getPlayer();
                    PlayerData playerData = this.api.getPlayerData(player.getUniqueId());
                    if (playerData == null) return;

                    PacketContainer packetContainer = event.getPacket();
                    List<WrappedChatComponent> components = packetContainer.getChatComponents().getValues();

                    for (int i = 0; i < components.size(); i++) {
                        WrappedChatComponent component = components.get(i);

                        if (component == null) continue;
                        if (component.getJson().equalsIgnoreCase("{\"text\":\"\"}")) continue;
                        if (!player.hasPermission("rosechat.deletemessages.client")
                                || i != components.size() - 1) {
                            playerData.getMessageLog().addDeletableMessage(new DeletableMessage(UUID.randomUUID(), component.getJson(), true));
                            continue;
                        }

                        ComponentBuilder builder = new ComponentBuilder();
                        builder.append(ComponentSerializer.parse(component.getJson()), ComponentBuilder.FormatRetention.NONE);
                        builder.append(" ", ComponentBuilder.FormatRetention.NONE);

                        UUID uuid = UUID.randomUUID();

                        RoseSender sender = new RoseSender(player);
                        BaseComponent[] deleteClient = MessageUtils.parseCustomPlaceholder(sender, sender, ConfigurationManager.Setting.DELETE_CLIENT_MESSAGES_FORMAT.getString(),
                                MessageUtils.getSenderViewerPlaceholders(sender, sender).addPlaceholder("id", uuid.toString()).build());

                        if (deleteClient == null) {
                            playerData.getMessageLog().addDeletableMessage(new DeletableMessage(UUID.randomUUID(), component.getJson(), true));
                            continue;
                        }

                        builder.append(deleteClient, ComponentBuilder.FormatRetention.NONE);
                        String json = ComponentSerializer.toString(builder.create());

                        playerData.getMessageLog().addDeletableMessage(new DeletableMessage(uuid, json, true));

                        component.setJson(json);
                        packetContainer.getChatComponents().write(components.indexOf(component), component);
                    }
                }
            }
        });
    }
}
