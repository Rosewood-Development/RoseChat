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
import dev.rosewood.rosechat.message.DeletableMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
                        builder.append(ComponentSerializer.parse(component.getJson()));
                        builder.append(" ", ComponentBuilder.FormatRetention.NONE);

                        UUID uuid = UUID.randomUUID();
                        BaseComponent[] deleteClient = TextComponent.fromLegacyText("✖");
                        for (BaseComponent deleteComponent : deleteClient) {
                            deleteComponent.setColor(ChatColor.of("#FFB39C"));
                            deleteComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to Delete")));
                            deleteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delmsg " + uuid.toString()));
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
