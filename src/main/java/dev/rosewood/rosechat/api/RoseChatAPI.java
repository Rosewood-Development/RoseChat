package dev.rosewood.rosechat.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.BungeeManager;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.manager.EmojiManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.manager.ReplacementManager;
import dev.rosewood.rosechat.manager.TagManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The API for the RoseChat plugin.
 */
public final class RoseChatAPI {

    private static RoseChatAPI instance;
    private final RoseChat plugin;
    private Class<?> spigotConfigClass;
    private Field bungeeField;

    private RoseChatAPI() {
        this.plugin = RoseChat.getInstance();
    }

    /**
     * @return The instance of the RoseChatAPI.
     */
    public static RoseChatAPI getInstance() {
        if (instance == null) instance = new RoseChatAPI();
        return instance;
    }

    /**
     * @return The current version of the plugin.
     */
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    /**
     * Parses a string into the RoseChat {@link MessageWrapper} and {@link MessageTokenizer}, allowing for hex color, tags and emoji in other text.
     * @param sender The person sending the message.
     * @param viewer The person receiving the message.
     * @param message The string to parse.
     * @return A {@link BaseComponent} consisting of the parsed message.
     */
    public BaseComponent[] parse(RoseSender sender, RoseSender viewer, String message) {
        return new MessageWrapper(sender, MessageLocation.NONE, null, message).parse(null, viewer);
    }

    /**
     * Parses a string into the RoseChat {@link MessageWrapper} and {@link MessageTokenizer}, allowing for hex color, tags and emoji in other text.
     * @param sender The person sending the message.
     * @param viewer The person receiving the message.
     * @param message The string to parse.
     * @param placeholders A set of placeholders to use.
     * @return A {@link BaseComponent} consisting of the parsed message.
     */
    public BaseComponent[] parse(RoseSender sender, RoseSender viewer, String message, StringPlaceholders placeholders) {
        return new MessageWrapper(sender, MessageLocation.NONE, null, message, placeholders).parse(null, viewer);
    }

    /**
     * Parses a string into the RoseChat {@link MessageWrapper} and {@link MessageTokenizer}, allowing for hex color, tags and emoji in other text.
     * @param location The location that the chat message is in.
     * @param sender The person sending the message.
     * @param viewer The person receiving the message.
     * @param message The string to parse.
     * @return A {@link BaseComponent} consisting of the parsed message.
     */
    public BaseComponent[] parse(MessageLocation location, RoseSender sender, RoseSender viewer, String message) {
        return new MessageWrapper(sender, location, null, message).parse(null, viewer);
    }

    /**
     * Creates a new chat channel.
     * @param id The ID to use.
     * @param format The format to use.
     * @return The new chat channel.
     */
    public ChatChannel createChannel(String id, String format) {
        ChatChannel channel = new ChatChannel(id, format);
        this.getChannelManager().addChannel(channel);
        return channel;
    }

    /**
     * Deletes a chat channel.
     * @param channel The channel to delete.
     */
    public void deleteChannel(ChatChannel channel) {
        this.getChannelManager().removeChannel(channel);
    }

    /**
     * @param id The ID to use.
     * @return The channel found, or null if it doesn't exist.
     */
    public ChatChannel getChannelById(String id) {
        return this.getChannelManager().getChannel(id);
    }

    /**
     * @return A list of all the chat channels.
     */
    public List<ChatChannel> getChannels() {
        return new ArrayList<>(this.getChannelManager().getChannels().values());
    }

    /**
     * @return A list of all the chat channel IDs.
     */
    public List<String> getChannelIDs() {
        return new ArrayList<>(this.getChannelManager().getChannels().keySet());
    }

    /**
     * Creates a new chat replacement.
     * @param id The ID to use.
     * @param text The text that will be replaced.
     * @param replacement The text to replace with.
     * @param regex Whether this replacement uses regex.
     * @return The new chat replacement.
     */
    public ChatReplacement createReplacement(String id, String text, String replacement, boolean regex) {
        ChatReplacement chatReplacement = new ChatReplacement(id, text, replacement, regex);
        this.getReplacementManager().addReplacement(chatReplacement);
        return chatReplacement;
    }

    /**
     * Deletes a chat replacement.
     * @param replacement The replacement to delete.
     */
    public void deleteReplacement(ChatReplacement replacement) {
        this.getReplacementManager().removeReplacement(replacement);
    }

    /**
     * @param id The ID to use.
     * @return The chat replacement found, or null if it doesn't exist.
     */
    public ChatReplacement getReplacementById(String id) {
        return this.getReplacementManager().getReplacement(id);
    }

    /**
     * @return A list of all chat replacements.
     */
    public List<ChatReplacement> getReplacements() {
        return new ArrayList<>(this.getReplacementManager().getReplacements().values());
    }

    /**
     * @return A list of all chat replacement IDs.
     */
    public List<String> getReplacementIDs() {
        return new ArrayList<>(this.getReplacementManager().getReplacements().keySet());
    }

    /**
     * Creates a new emoji.
     * @param id The ID to use.
     * @param text The text to be replaced.
     * @param replacement The text to replace with.
     * @param hoverText The text shown when the replacement is hovered over.
     * @param font The font to use for the emoji.
     * @return The new emoji.
     */
    public ChatReplacement createEmoji(String id, String text, String replacement, String hoverText, String font) {
        ChatReplacement chatReplacement = new ChatReplacement(id, text, replacement, hoverText, font, false);
        this.getEmojiManager().addEmoji(chatReplacement);
        return chatReplacement;
    }

    /**
     * Deletes an emoji.
     * @param emoji The emoji to delete.
     */
    public void deleteEmoji(ChatReplacement emoji) {
        this.getEmojiManager().removeEmoji(emoji);
    }

    /**
     * @param id The ID to use.
     * @return The emoji found, or null if it doesn't exist.
     */
    public ChatReplacement getEmojiById(String id) {
        return this.getEmojiManager().getEmoji(id);
    }

    /**
     * @return A list of all emojis, specified in emojis.yml.
     */
    public List<ChatReplacement> getEmojis() {
        return new ArrayList<>(this.getEmojiManager().getEmojis().values());
    }

    /**
     * @return A list of all emoji IDs.
     */
    public List<String> getEmojiIds() {
        return new ArrayList<>(this.getEmojiManager().getEmojis().keySet());
    }

    /**
     * Creates a new group chat.
     * @param id The ID of the group chat.
     * @param owner The owner of the group chat.
     * @return The new group chat.
     */
    public GroupChat createGroupChat(String id, UUID owner) {
        GroupChat groupChat = new GroupChat(id);
        groupChat.setOwner(owner);
        groupChat.addMember(owner);
        this.getGroupManager().addGroupChat(groupChat);
        this.getGroupManager().addMember(groupChat, owner);
        return groupChat;
    }

    /**
     * Deletes a group chat.
     * @param groupChat The group chat to delete.
     */
    public void deleteGroupChat(GroupChat groupChat) {
        this.getGroupManager().removeGroupChat(groupChat);
        this.getGroupManager().deleteGroupChat(groupChat);
    }

    /**
     * Adds a player to a group chat.
     * @param groupChat The group chat to be added to.
     * @param member The player to be added.
     */
    public void addGroupChatMember(GroupChat groupChat, Player member) {
        groupChat.addMember(member);
        this.getGroupManager().addMember(groupChat, member.getUniqueId());
    }

    /**
     * Removes a player from a group chat.
     * @param groupChat The group chat to be removed from.
     * @param member The player to be removed.
     */
    public void removeGroupChatMember(GroupChat groupChat, Player member) {
        groupChat.removeMember(member);
        this.getGroupManager().removeMember(groupChat, member.getUniqueId());
    }

    /**
     * @param owner The owner to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChat getGroupChatByOwner(UUID owner) {
        return this.getGroupManager().getGroupChatByOwner(owner);
    }

    /**
     * @param id The ID to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChat getGroupChatById(String id) {
        return this.getGroupManager().getGroupChatById(id);
    }

    /**
     * @return A list of all group chats.
     */
    public List<GroupChat> getGroupChats() {
        return new ArrayList<>(this.getGroupManager().getGroupChats().values());
    }

    /**
     * @param player The UUID of the player to use.
     * @return A list of all group chats that the player is in.
     */
    public List<GroupChat> getGroupChats(UUID player) {
        return this.getGroupManager().getGroupChats().values().stream().filter(gc -> gc.getMembers().contains(player)).collect(Collectors.toList());
    }

    /**
     * @return A list of all group chat IDs.
     */
    public List<String> getGroupChatIDs() {
        return new ArrayList<>(this.getGroupManager().getGroupChats().keySet());
    }

    /**
     * Creates a new tag.
     * @param id The ID to use.
     * @return The new tag.
     */
    public Tag createTag(String id) {
        Tag tag = new Tag(id);
        this.getTagManager().addTag(tag);
        return tag;
    }

    /**
     * Deletes a tag.
     * @param tag The tag to delete
     */
    public void deleteTag(Tag tag) {
        this.getTagManager().removeTag(tag);
    }

    /**
     * @param id The id to use.
     * @return The tag found, or null if it doesn't exist.
     */
    public Tag getTagById(String id) {
        return this.getTagManager().getTag(id);
    }

    /**
     * @return A list of all tags.
     */
    public List<Tag> getTags() {
        return new ArrayList<>(this.getTagManager().getTags().values());
    }

    /**
     * @return A list of all tag IDs.
     */
    public List<String> getTagIDs() {
        return new ArrayList<>(this.getTagManager().getTags().keySet());
    }

    /**
     * @param uuid The uuid of the player whose data should be got.
     * @return The data of the player.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return this.getPlayerDataManager().getPlayerData(uuid);
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
     * @return An instance of the emoji manager.
     */
    public EmojiManager getEmojiManager() {
        return this.plugin.getManager(EmojiManager.class);
    }

    /**
     * @return An instance of the replacement manager.
     */
    public ReplacementManager getReplacementManager() {
        return this.plugin.getManager(ReplacementManager.class);
    }

    /**
     * @return An instance of the tag manager.
     */
    public TagManager getTagManager() {
        return this.plugin.getManager(TagManager.class);
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
