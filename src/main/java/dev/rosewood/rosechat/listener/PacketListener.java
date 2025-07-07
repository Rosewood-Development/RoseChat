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
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.utils.NMSUtil;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

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

        ListenerPriority priority =  ListenerPriority.NORMAL;
        try {
            priority = ListenerPriority.valueOf(Settings.PACKET_EVENT_PRIORITY.get());
        } catch (IllegalArgumentException ignored) {

        }

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, priority, (NMSUtil.getVersionNumber() >= 19 ? types : legacyTypes)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!Settings.ENABLE_DELETING_MESSAGES.get())
                    return;

                RosePlayer player = new RosePlayer(event.getPlayer());
                PlayerData data = player.getPlayerData();
                if (data == null)
                    return;

                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    PacketContainer packet = event.getPacket();

                    WrappedChatComponent chatComponent = packet.getChatComponents().readSafely(0);
                    String messageJson = chatComponent == null ? getMessageReflectively(packet) : chatComponent.getJson();

                    if (messageJson == null || messageJson.equalsIgnoreCase("{\"text\":\"\"}"))
                        return;

                    // Ensures chat messages are added separately, to differentiate between client and server messages.
                    DeletableMessage loggedMessage = data.getMessageLog().getDeletableMessage(messageJson);
                    if (loggedMessage != null) {
                        if (loggedMessage.isClient() || loggedMessage.getOriginal() != null)
                            return;

                        // Updated the logged message to keep the unedited message and the new edited message.
                        loggedMessage.setOriginal(loggedMessage.getJson());
                        String json = getServerMessageJson(loggedMessage, player);
                        if (json == null)
                            return;

                        loggedMessage.setJson(json);
                        event.setPacket(createSystemPacket(json));
                        return;
                    }

                    UUID messageId = UUID.randomUUID();
                    if (updateAndCheckPermissions(player, messageId, messageJson))
                        return;

                    BaseComponent[] deleteClientButton = MessageUtils.appendDeleteButton(player, data, messageId.toString(), messageJson);

                    if (deleteClientButton == null)
                        return;

                    DeletableMessage deletableMessage = new DeletableMessage(messageId, messageJson, true, null);
                    deletableMessage.setOriginal(messageJson);

                    messageJson = ComponentSerializer.toString(deleteClientButton);
                    deletableMessage.setJson(messageJson);
                    data.getMessageLog().addDeletableMessage(deletableMessage);

                    // Overwrite the packet since packet fields are final in 1.19
                    PacketContainer newPacket = new PacketContainer(PacketType.Play.Server.CHAT);
                    newPacket.getChatComponents().write(0, WrappedChatComponent.fromJson(messageJson));
                    event.setPacket(newPacket);
                } else if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT) {
                    PacketContainer packet = event.getPacket();
                    if (packet.getBooleans().size() == 0 || packet.getBooleans().readSafely(0)) // Ignore hotbar messages
                        return;

                    String messageJson;

                    // Use a chat component on 1.20.4
                    if (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 4)) {
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

                    // Workaround for https://hub.spigotmc.org/jira/browse/SPIGOT-7563
                    BaseComponent[] components = null;
                    try {
                        components = messageJson == null ? null : ComponentSerializer.parse(messageJson);
                    } catch (JsonSyntaxException ignored) {}

                    if (components == null || components.length == 0 || components[0].toPlainText().trim().isEmpty())
                        return;

                    // Ensures chat messages are added separately, to differentiate between client and server messages.
                    DeletableMessage loggedMessage = data.getMessageLog().getDeletableMessage(messageJson);
                    if (loggedMessage != null) {
                        if (loggedMessage.isClient() || loggedMessage.getOriginal() != null)
                            return;

                        // Updated the logged message to keep the unedited message and the new edited message.
                        loggedMessage.setOriginal(loggedMessage.getJson());
                        String json = getServerMessageJson(loggedMessage, player);
                        if (json == null)
                            return;

                        loggedMessage.setJson(json);
                        event.setPacket(createSystemPacket(json));
                        return;
                    }

                    UUID messageId = UUID.randomUUID();
                    if (updateAndCheckPermissions(player, messageId, messageJson))
                        return;

                    // Work around for https://hub.spigotmc.org/jira/browse/SPIGOT-7563
                    BaseComponent[] deleteClientButton = null;
                    try {
                        deleteClientButton = MessageUtils.appendDeleteButton(player, data, messageId.toString(), messageJson);
                    } catch (JsonSyntaxException ignored) {}

                    if (deleteClientButton == null)
                        return;

                    DeletableMessage deletableMessage = new DeletableMessage(messageId, messageJson, true, null);
                    deletableMessage.setOriginal(messageJson);

                    messageJson = ComponentSerializer.toString(deleteClientButton);
                    deletableMessage.setJson(messageJson);
                    data.getMessageLog().addDeletableMessage(deletableMessage);

                    event.setPacket(createSystemPacket(messageJson));
                }
            }
        });
    }

    private boolean updateAndCheckPermissions(RosePlayer player, UUID messageId, String messageJson) {
        if (!permissionsCache.asMap().containsKey(player.getUUID()))
            permissionsCache.put(player.getUUID(), player.hasPermission("rosechat.deletemessages.client"));

        if (!permissionsCache.getIfPresent(player.getUUID())) {
            player.getPlayerData().getMessageLog().addDeletableMessage(new DeletableMessage(messageId, messageJson, true, null));
            return true;
        }

        if (!groupCache.asMap().containsKey(player.getUUID())) {
            Permission vault = RoseChatAPI.getInstance().getVault();
            if (vault != null) {
                String group = player.getPermissionGroup();
                groupCache.put(player.getUUID(), group);
            }
        }

        String group = groupCache.getIfPresent(player.getUUID());
        player.setPermissionGroup(group == null ? "default" : group);
        return false;
    }

    private PacketContainer createSystemPacket(String json) {
        // Overwrite the packet since packet fields are final in 1.19
        PacketContainer packet = new PacketContainer(NMSUtil.getVersionNumber() >= 19 ? PacketType.Play.Server.SYSTEM_CHAT : PacketType.Play.Server.CHAT);

        if (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 4)) {
            packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
        } else if (NMSUtil.getVersionNumber() == 19) {
            packet.getStrings().write(0, json);
        } else {
            packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
        }

        return packet;
    }

    private String getServerMessageJson(DeletableMessage message, RosePlayer viewer) {
        BaseComponent[] components = ComponentSerializer.parse(message.getJson());
        if (components == null || components.length == 0 || viewer.isConsole())
            return null;

        boolean isSamePlayer = message.getSender() != null && message.getSender().equals(viewer.getUUID());
        String permission = isSamePlayer ? "rosechat.deletemessages.self" : "rosechat.deletemessages.others";
        String format = isSamePlayer ? Settings.DELETE_OWN_MESSAGE_FORMAT.get() :  Settings.DELETE_OTHER_MESSAGE_FORMAT.get();

        if (!viewer.hasPermission(permission))
            return null;

        BaseComponent[] appended = this.appendDeleteButton(components, message, viewer, format);
        return ComponentSerializer.toString(appended);
    }

    private BaseComponent[] appendDeleteButton(BaseComponent[] components, DeletableMessage message, RosePlayer viewer, String placeholder) {
        BaseComponent[] button = this.getDeleteButton(message, viewer, placeholder);

        ComponentBuilder builder = new ComponentBuilder();
        if (Settings.DELETE_MESSAGE_SUFFIX.get()) {
            builder.append(components, ComponentBuilder.FormatRetention.NONE);

            if (button != null)
                builder.append(button);
        } else {
            if (button != null)
                builder.append(button);

            builder.append(components, ComponentBuilder.FormatRetention.NONE);
        }

        return builder.create();
    }

    private BaseComponent[] getDeleteButton(DeletableMessage message, RosePlayer viewer, String placeholder) {
        RosePlayer sender = new RosePlayer(message.getSender() == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(message.getSender()));

        return RoseChatAPI.getInstance().parse(new RosePlayer(Bukkit.getConsoleSender()), viewer, placeholder,
                DefaultPlaceholders.getFor(sender, viewer)
                        .add("id", message.getUUID().toString())
                        .add("type", "server")
                        .add("channel", message.getChannel())
                        .build()).buildComponents();
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
