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
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.util.UUID;

public class PacketListener {

    private static Field componentsArrayField;
    private static Field adventureMessageField;

    public PacketListener(RoseChat plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            final RoseChatAPI api = RoseChatAPI.getInstance();

            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.CHAT) return;
                Player player = event.getPlayer();
                PlayerData playerData = this.api.getPlayerData(player.getUniqueId());
                if (playerData == null) return;

                PacketContainer packet = event.getPacket();
                String messageJson;

                WrappedChatComponent chatComponent = packet.getChatComponents().readSafely(0);
                if (chatComponent == null) {
                    messageJson = getMessageReflectively(packet);
                } else {
                    messageJson = chatComponent.getJson();
                }

                if (messageJson == null || messageJson.equalsIgnoreCase("{\"text\":\"\"}")) return;

                // Ensures chat messages are added separately, to differentiate between client and server messages.
                if (playerData.getMessageLog().containsDeletableMessage(messageJson)) return;
                UUID messageId = UUID.randomUUID();

                if (!player.hasPermission("rosechat.deletemessages.client")) {
                    playerData.getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true));
                    return;
                }

                RoseSender sender = new RoseSender(player);
                BaseComponent[] deleteClientButton = appendButton(sender, playerData, messageId.toString(), messageJson);

                if (deleteClientButton == null) return;

                messageJson = ComponentSerializer.toString(deleteClientButton);
                if (!setMessageReflectively(packet, messageJson)) chatComponent.setJson(messageJson);
                playerData.getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true));
            }
        });
    }

    // Allow the client message to be deletable.
    public static BaseComponent[] appendButton(RoseSender sender, PlayerData playerData, String messageId, String messageJson) {
        ComponentBuilder builder = new ComponentBuilder();
        String placeholder = Setting.DELETE_CLIENT_MESSAGE_FORMAT.getString();
        BaseComponent[] deleteClientButton = MessageUtils.parseCustomPlaceholder(sender, sender, placeholder.substring(1, placeholder.length() - 1),
                MessageUtils.getSenderViewerPlaceholders(sender, sender)
                        .addPlaceholder("id", messageId)
                        .addPlaceholder("type", "client")
                        .addPlaceholder("message", "").build(), false);

        if (deleteClientButton == null) {
            playerData.getMessageLog().addDeletableMessage(new DeletableMessage(UUID.randomUUID(), messageJson, true));
            return null;
        }

        if (shouldSuffixButton(sender, placeholder)) {
            builder.append(ComponentSerializer.parse(messageJson), ComponentBuilder.FormatRetention.NONE);
            builder.append(deleteClientButton, ComponentBuilder.FormatRetention.NONE);
        } else {
            builder.append(deleteClientButton, ComponentBuilder.FormatRetention.NONE);
            builder.append(ComponentSerializer.parse(messageJson), ComponentBuilder.FormatRetention.NONE);
        }

        return builder.create();
    }

    private static boolean shouldSuffixButton(RoseSender sender, String placeholderId) {
        CustomPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholderId.substring(1, placeholderId.length() - 1));
        if (placeholder == null) return false;

        String text = placeholder.getText().parse(sender, sender,
                MessageUtils.getSenderViewerPlaceholders(sender, sender)
                        .addPlaceholder("type", "client").build());
        return text.trim().startsWith("%message%");
    }

    // Thanks, Nicole!
    private String getMessageReflectively(PacketContainer packet) {
        // Assume this is a component array message, need to do some reflective nonsense to get it into json
        try {
            if (componentsArrayField == null) {
                try {
                    componentsArrayField = packet.getHandle().getClass().getDeclaredField("components");
                    componentsArrayField.setAccessible(true);
                } catch (ReflectiveOperationException ignored) {
                    return null;
                }
            }

            return ComponentSerializer.toString((BaseComponent[]) componentsArrayField.get(packet.getHandle()));
        } catch (Exception ignored) {}

        // Welp, that didn't work
        // Assume this is an Adventure chat message, need to do some other reflective nonsense to get it into json
        try {
            if (adventureMessageField == null) {
                try {
                    adventureMessageField = packet.getHandle().getClass().getDeclaredField("adventure$message");
                    adventureMessageField.setAccessible(true);
                } catch (ReflectiveOperationException ignored) {
                    return null;
                }
            }

            GsonComponentSerializer serializer = GsonComponentSerializer.builder().build();
            return serializer.serialize((Component) adventureMessageField.get(packet.getHandle()));
        } catch (Exception ignored) {}

        return null;
    }

    private boolean setMessageReflectively(PacketContainer packet, String json) {
        try {
            if (componentsArrayField != null) {
                componentsArrayField.set(packet.getHandle(), ComponentSerializer.parse(json));
                return true;
            }
        } catch (IllegalAccessException ignored) {}

        try {
            if (adventureMessageField != null) {
                GsonComponentSerializer serializer = GsonComponentSerializer.builder().build();
                adventureMessageField.set(packet.getHandle(), serializer.deserialize(json));
                return true;
            }
        } catch (IllegalAccessException ignored) {}
        return false;
    }
}
