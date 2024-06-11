package dev.rosewood.rosechat.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonSyntaxException;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.NMSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PacketListener {

    private static Field componentsArrayField;
    private static Field adventureMessageField;

    private static Field contentField;
    private static Field adventureContentField;

    private final Cache<UUID, Boolean> permissionsCache;
    private final Cache<UUID, String> groupCache;

    private final RoseChat plugin;

    public PacketListener(RoseChat plugin) {
        this.plugin = plugin;

        this.permissionsCache = CacheBuilder.newBuilder()
                        .expireAfterWrite(5, TimeUnit.SECONDS).build();
        this.groupCache = CacheBuilder.newBuilder()
                        .expireAfterWrite(5, TimeUnit.SECONDS).build();
    }

    public void removeListeners() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this.plugin);
    }

    public void addListener() {
        PacketType[] legacyTypes = new PacketType[]{ PacketType.Play.Server.CHAT };
        PacketType[] types = new PacketType[]{ PacketType.Play.Server.CHAT, PacketType.Play.Server.SYSTEM_CHAT };

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, (NMSUtil.getVersionNumber() >= 19 ? types : legacyTypes)) {
            final RoseChatAPI api = RoseChatAPI.getInstance();

            @Override
            public void onPacketSending(PacketEvent event) {
                if (!Setting.ENABLE_DELETING_MESSAGES.getBoolean())
                    return;

                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    Player player = event.getPlayer();
                    PlayerData playerData = this.api.getPlayerData(player.getUniqueId());
                    if (playerData == null)
                        return;

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

                    if (!permissionsCache.asMap().containsKey(player.getUniqueId()))
                        permissionsCache.put(player.getUniqueId(), player.hasPermission("rosechat.deletemessages.client"));

                    if (!permissionsCache.getIfPresent(player.getUniqueId())) {
                        playerData.getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true));
                        return;
                    }

                    RosePlayer sender = new RosePlayer(player);
                    if (!groupCache.asMap().containsKey(player.getUniqueId())) {
                        Permission vault = this.api.getVault();
                        if (vault != null) {
                            String group = this.api.getVault().getPrimaryGroup(player);
                            groupCache.put(player.getUniqueId(), group);
                        }
                    }

                    String group = groupCache.getIfPresent(player.getUniqueId());
                    sender.setPermissionGroup(Objects.requireNonNullElse(group, "default"));

                    BaseComponent[] deleteClientButton = MessageUtils.appendDeleteButton(sender, playerData, messageId.toString(), messageJson);

                    if (deleteClientButton == null)
                        return;

                    messageJson = ComponentSerializer.toString(deleteClientButton);
                    playerData.getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true));

                    // Overwrite the packet since packet fields are final in 1.19
                    PacketContainer newPacket = new PacketContainer(PacketType.Play.Server.CHAT);
                    newPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(messageJson));
                    event.setPacket(newPacket);
                } else if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT) {
                    Player player = event.getPlayer();
                    PlayerData playerData = this.api.getPlayerData(player.getUniqueId());
                    if (playerData == null)
                        return;

                    PacketContainer packet = event.getPacket();
                    if (packet.getBooleans().size() == 0 || packet.getBooleans().readSafely(0)) // Ignore hotbar messages
                        return;

                    String messageJson;

                    // Use a chat component on 1.20.4
                    if (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 4) {
                        WrappedChatComponent chatComponent = packet.getChatComponents().readSafely(0);
                        if (chatComponent == null) {
                            messageJson = getMessageReflectively(packet);
                        } else {
                            messageJson = chatComponent.getJson();
                        }
                    } else {
                        String jsonString = packet.getStrings().readSafely(0);
                        if (jsonString != null) {
                            messageJson = jsonString;
                        } else {
                            messageJson = getMessageReflectively(packet);
                        }
                    }

                    // Work around for https://hub.spigotmc.org/jira/browse/SPIGOT-7563
                    BaseComponent[] components = null;
                    try {
                        components = messageJson == null ? null : ComponentSerializer.parse(messageJson);
                    } catch (JsonSyntaxException ignored) {}

                    if (components == null || components.length == 0 || components[0].toPlainText().trim().isEmpty())
                        return;

                    // Ensures chat messages are added separately, to differentiate between client and server messages.
                    if (playerData.getMessageLog().containsDeletableMessage(messageJson))
                        return;
                    UUID messageId = UUID.randomUUID();

                    if (!permissionsCache.asMap().containsKey(player.getUniqueId()))
                        permissionsCache.put(player.getUniqueId(), player.hasPermission("rosechat.deletemessages.client"));

                    if (!permissionsCache.getIfPresent(player.getUniqueId())) {
                        playerData.getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true));
                        return;
                    }

                    RosePlayer sender = new RosePlayer(player);
                    if (!groupCache.asMap().containsKey(player.getUniqueId())) {
                        Permission vault = this.api.getVault();
                        if (vault != null) {
                            String group = this.api.getVault().getPrimaryGroup(player);
                            groupCache.put(player.getUniqueId(), group);
                        }
                    }

                    String group = groupCache.getIfPresent(player.getUniqueId());
                    sender.setPermissionGroup(Objects.requireNonNullElse(group, "default"));

                    // Work around for https://hub.spigotmc.org/jira/browse/SPIGOT-7563
                    BaseComponent[] deleteClientButton = null;
                    try {
                        deleteClientButton = MessageUtils.appendDeleteButton(sender, playerData, messageId.toString(), messageJson);
                    } catch (JsonSyntaxException ignored) {}

                    if (deleteClientButton == null)
                        return;

                    messageJson = ComponentSerializer.toString(deleteClientButton);
                    playerData.getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true));

                    // Overwrite the packet since packet fields are final in 1.19
                    PacketContainer newPacket = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);

                    if (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 4) {
                        newPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(messageJson));
                    } else {
                        newPacket.getStrings().write(0, messageJson);
                    }


                    event.setPacket(newPacket);
                }
            }
        });
    }

    // Thanks, Nicole!
    private String getMessageReflectively(PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Server.CHAT) {
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
                    } catch (ReflectiveOperationException e) {
                        return null;
                    }
                }

                GsonComponentSerializer serializer = GsonComponentSerializer.builder().build();
                return serializer.serialize((Component) adventureMessageField.get(packet.getHandle()));
            } catch (Exception ignored) {}
        } else if (packet.getType() == PacketType.Play.Server.SYSTEM_CHAT) {
            // Assume this is a normal json message
            try {
                if (contentField == null) {
                    try {
                        contentField = packet.getHandle().getClass().getDeclaredField("content");
                        contentField.setAccessible(true);
                    } catch (ReflectiveOperationException ignored) {}
                }

                String value = (String) contentField.get(packet.getHandle());
                if (value != null)
                    return value;
            } catch (Exception ignored) {}

            // Welp, that didn't work
            // Assume this is an Adventure chat message, need to do some other reflective nonsense to get it into json
            try {
                if (adventureContentField == null) {
                    try {
                        adventureContentField = packet.getHandle().getClass().getDeclaredField("adventure$content");
                        adventureContentField.setAccessible(true);
                    } catch (ReflectiveOperationException e) {
                        return null;
                    }
                }

                GsonComponentSerializer serializer = GsonComponentSerializer.builder().build();
                return serializer.serialize((Component) adventureContentField.get(packet.getHandle()));
            } catch (Exception ignored) {}
        }

        return null;
    }

}
