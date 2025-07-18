package dev.rosewood.rosechat.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.BungeeManager;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.manager.FilterManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.RoseMessage;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

/**
 * The API for the RoseChat plugin.
 */
@SuppressWarnings("unused")
public final class RoseChatAPI {

    private static RoseChatAPI instance;
    private final RoseChat plugin;
    private Class<?> spigotConfigClass;
    private Field bungeeField;
    private Class<?> paperConfigClass;
    private Object velocityObject;
    private Field velocityEnabledField;

    private RoseChatAPI() {
        this.plugin = RoseChat.getInstance();
    }

    /**
     * @return The instance of the RoseChatAPI.
     */
    public static RoseChatAPI getInstance() {
        if (instance == null)
            instance = new RoseChatAPI();

        return instance;
    }

    /**
     * @return The current version of the plugin.
     */
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    /**
     * Parses a string allowing for hex color, tags and emoji in other text.
     * @param sender The {@link RosePlayer} sending the message.
     * @param viewer The {@link RosePlayer} receiving the message.
     * @param format The string to parse.
     * @param placeholders A set of {@link StringPlaceholders} to be parsed in the message.
     * @return A {@link MessageContents} consisting of the parsed message.
     */
    public MessageContents parse(RosePlayer sender, RosePlayer viewer, String format, StringPlaceholders placeholders) {
        RoseMessage roseMessage = RoseMessage.forLocation(sender, PermissionArea.NONE);
        roseMessage.setPlaceholders(placeholders);

        return roseMessage.parse(viewer, format);
    }

    /**
     * Parses a string allowing for hex color, tags and emoji in other text.
     * @param sender The {@link RosePlayer} sending the message.
     * @param viewer The {@link RosePlayer} receiving the message.
     * @param format The string to parse.
     * @return A {@link MessageContents} consisting of the parsed message.
     */
    public MessageContents parse(RosePlayer sender, RosePlayer viewer, String format) {
        return RoseMessage.forLocation(sender, PermissionArea.NONE).parse(viewer, format);
    }

    /**
     * Parses a string allowing for hex color, tags and emoji in other text.
     * @param sender The {@link RosePlayer} sending the message.
     * @param viewer The {@link RosePlayer} receiving the message.
     * @param format The string to parse.
     * @param location The location that the chat message is in.
     * @return A {@link MessageContents} consisting of the parsed message.
     */
    public MessageContents parse(RosePlayer sender, RosePlayer viewer, String format, PermissionArea location) {
        return RoseMessage.forLocation(sender, location).parse(viewer, format);
    }

    /**
     * Deletes a chat message with the given UUID.
     * @param player The {@link RosePlayer} to delete the message for.
     * @param uuid The {@link UUID} of the message.
     */
    public void deleteMessage(RosePlayer player, UUID uuid) {
        DeletableMessage messageToDelete = null;

        // Find the message.
        for (DeletableMessage message : player.getPlayerData().getMessageLog().getDeletableMessages()) {
            if (message.getUUID().equals(uuid))
                messageToDelete = message;
        }

        if (messageToDelete == null)
            return;

        // Get the deleted message format.
        MessageContents format = this.parse(player, player, Settings.DELETED_MESSAGE_FORMAT.get(),
                DefaultPlaceholders.getFor(player, player)
                        .add("id", uuid.toString())
                        .add("type", messageToDelete.isClient() ? "client" : "server")
                        .build());

        String plainText = format.build(ChatComposer.plain());

        boolean updated = false;
        if (!plainText.isEmpty()) {
            String json;
            if (player.hasPermission("rosechat.deletemessages.see")) {
                ComponentBuilder builder = new ComponentBuilder();
                BaseComponent[] messageComponents = format.build(ChatComposer.decorated());
                builder.append(messageComponents, ComponentBuilder.FormatRetention.NONE);
                if (builder.getCurrentComponent().getHoverEvent() == null) {
                    builder.getCurrentComponent().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComposer.decorated().composeJson(messageToDelete.getOriginal())));
                    json = ChatComposer.json().composeBungee(new BaseComponent[]{builder.build()});
                } else {
                    json = ChatComposer.json().composeBungee(messageComponents);
                }
            } else {
                json = format.build(ChatComposer.json());
            }

            if (player.hasPermission("rosechat.deletemessages.client")) {
                BaseComponent[] withDeleteButton = MessageUtils.appendDeleteButton(player, player.getPlayerData(), uuid.toString(), json);

                if (withDeleteButton != null) {
                    messageToDelete.setJson(ChatComposer.json().composeBungee(withDeleteButton));
                } else {
                    // If the delete button doesn't exist, just use the 'Deleted Message' message.
                    messageToDelete.setJson(json);
                }
            } else {
                // If the player doesn't have permission, just use the 'Deleted Message' message.
                messageToDelete.setJson(json);
            }

            updated = true;
        }

        // Remove the original message if it was not changed.
        if (!updated)
            player.getPlayerData().getMessageLog().getDeletableMessages().remove(messageToDelete);

        // Send blank lines to clear the chat.
        for (int i = 0; i < 100; i++)
            player.send("\n");

        // Resend the messages!
        for (DeletableMessage message : player.getPlayerData().getMessageLog().getDeletableMessages())
            player.send(ChatComposer.decorated().composeJson(message.getJson()));

        // If the message is not a client message, delete it from Discord too.
        if (!messageToDelete.isClient()) {
            if (updated)
                messageToDelete.setClient(true);

            if (!Settings.DELETE_DISCORD_MESSAGES.get())
                return;

            if (this.getDiscord() != null && messageToDelete.getDiscordId() != null)
                this.getDiscord().deleteMessage(messageToDelete.getDiscordId());
        }
    }

    /**
     * Creates a new chat channel.
     * @param provider The {@link ChannelProvider} for the channel.
     * @param id The ID to use.
     * @return The new chat channel, may return null if failed to register properly.
     */
    public Channel createChannel(ChannelProvider provider, String id) {
        provider.generateDynamicChannel(id);
        return this.getChannelManager().getChannel(id);
    }

    /**
     * Deletes a chat channel.
     * @param id The ID of the channel.
     */
    public void deleteChannel(String id) {
        this.getChannelManager().deleteChannel(id);
    }

    /**
     * @param id The ID to use.
     * @return The channel found, or null if it doesn't exist.
     */
    public Channel getChannelById(String id) {
        return this.getChannelManager().getChannel(id);
    }

    /**
     * @return A list of all the chat channels.
     */
    public List<Channel> getChannels() {
        return new ArrayList<>(this.getChannelManager().getChannels().values());
    }

    /**
     * @return A list of all the chat channel IDs.
     */
    public List<String> getChannelIDs() {
        return new ArrayList<>(this.getChannelManager().getChannels().keySet());
    }

    /**
     * @return The default chat channel.
     */
    public Channel getDefaultChannel() {
        return this.getChannelManager().getDefaultChannel();
    }

    /**
     * Creates a new filter.
     * @param filter The {@link Filter} to register.
     * @return The new filter.
     */
    public Filter createFilter(Filter filter) {
        this.getFilterManager().addFilter(filter.id(), filter);
        return filter;
    }

    /**
     * Deletes a filter.
     * @param filter The {@link Filter} to delete.
     */
    public void deleteFilter(Filter filter) {
        this.getFilterManager().deleteFilter(filter.id());
    }

    /**
     * @param id The ID to use.
     * @return The filter found, or null if it doesn't exist.
     */
    public Filter getFilterById(String id) {
        return this.getFilterManager().getFilter(id);
    }

    /**
     * @return A list of all filters.
     */
    public List<Filter> getFilters() {
        return new ArrayList<>(this.getFilterManager().getFilters().values());
    }

    /**
     * @return A list of all filter IDs.
     */
    public List<String> getFilterIDs() {
        return new ArrayList<>(this.getFilterManager().getFilters().keySet());
    }

    /**
     * Creates a new group chat.
     * @param id The ID of the group chat.
     * @param owner The owner of the group chat.
     * @return The new group chat.
     */
    public GroupChannel createGroupChat(String id, Player owner) {
        GroupChannel group = new GroupChannel(id);
        group.setOwner(owner.getUniqueId());
        group.getMembers().add(owner.getUniqueId());

        this.getGroupManager().addGroupChat(group);
        this.getGroupManager().addMember(group, owner.getUniqueId());

        return group;
    }

    /**
     * Deletes a group chat.
     * @param group The {@link GroupChannel} to delete.
     */
    public void deleteGroupChat(GroupChannel group) {
        this.getGroupManager().deleteGroupChat(group);
    }

    /**
     * @param owner The owner to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChannel getGroupChatByOwner(UUID owner) {
        return this.getGroupManager().getGroupChatByOwner(owner);
    }

    /**
     * @param id The ID to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChannel getGroupChatById(String id) {
        return this.getGroupManager().getGroupChatById(id);
    }

    /**
     * @return A list of all group chat names.
     */
    public List<String> getGroupChatIDs() {
        return this.getGroupManager().getGroupChatIDs();
    }

    /**
     * @return A list of all group chats.
     */
    public List<GroupChannel> getGroupChats() {
        return new ArrayList<>(this.getGroupManager().getGroupChats().values());
    }

    /**
     * @param player The UUID of the player to use.
     * @return A list of all group chats that the player is in.
     */
    public List<GroupChannel> getGroupChats(UUID player) {
        return this.getGroupManager().getGroupChats().values().stream().filter(gc ->
                gc.getMembers().contains(player)).collect(Collectors.toList());
    }

    /**
     * @param uuid The uuid of the player whose data should be got.
     * @return The data of the player.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return this.getPlayerDataManager().getPlayerData(uuid);
    }

    /**
     * @param uuid The uuid of the player whose data should be got.
     * @param callback A consumer containing the PlayerData.
     */
    public void getPlayerData(UUID uuid, Consumer<PlayerData> callback) {
        this.getPlayerDataManager().getPlayerData(uuid, callback);
    }

    /**
     * @return An instance of the locale manager.
     */
    public LocaleManager getLocaleManager() {
        return this.plugin.getManager(LocaleManager.class);
    }

    /**
     * @return An instance of the player data manager.
     */
    public PlayerDataManager getPlayerDataManager() {
        return this.plugin.getManager(PlayerDataManager.class);
    }

    /**
     * @return An instance of the group manager.
     */
    public GroupManager getGroupManager() {
        return this.plugin.getManager(GroupManager.class);
    }

    /**
     * @return An instance of the channel manager.
     */
    public ChannelManager getChannelManager() {
        return this.plugin.getManager(ChannelManager.class);
    }

    /**
     * @return An instance of the placeholder manager.
     */
    public PlaceholderManager getPlaceholderManager() {
        return this.plugin.getManager(PlaceholderManager.class);
    }


    /**
     * @return An instance of the filter manager.
     */
    public FilterManager getFilterManager() {
        return this.plugin.getManager(FilterManager.class);
    }

    /**
     * @return An instance of the discord emoji manager.
     */
    public DiscordEmojiManager getDiscordEmojiManager() {
        return this.plugin.getManager(DiscordEmojiManager.class);
    }

    /**
     * @return An instance of the bungee manager.
     */
    public BungeeManager getBungeeManager() {
        return this.plugin.getManager(BungeeManager.class);
    }

    /**
     * @return An instance of the Permission class from Vault.
     */
    public Permission getVault() {
        return this.plugin.getVault();
    }

    /**
     * @return An instance of DiscordSRV.
     */
    public DiscordChatProvider getDiscord() {
        return this.plugin.getDiscord();
    }

    /**
     * @return True if the server is on BungeeCord.
     */
    public boolean isBungee() {
        if (NMSUtil.isPaper()) {
            if (this.paperConfigClass == null || this.velocityObject == null || this.velocityEnabledField == null) {
                try {
                    this.paperConfigClass = Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
                    Field paperConfigField = this.paperConfigClass.getDeclaredField("instance");
                    paperConfigField.setAccessible(true);
                    Object instance = paperConfigField.get(null);
                    Object proxies = instance.getClass().getDeclaredField("proxies").get(instance);
                    this.velocityObject = proxies.getClass().getDeclaredField("velocity").get(proxies);
                    this.velocityEnabledField = this.velocityObject.getClass().getDeclaredField("enabled");
                    return this.velocityEnabledField.getBoolean(this.velocityObject);
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    return this.velocityEnabledField.getBoolean(this.velocityObject);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        if (this.spigotConfigClass == null || this.bungeeField == null) {
            try {
                this.spigotConfigClass = Class.forName("org.spigotmc.SpigotConfig");
                this.bungeeField = this.spigotConfigClass.getDeclaredField("bungee");

                return this.bungeeField.getBoolean(null);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return this.bungeeField.getBoolean(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

}
