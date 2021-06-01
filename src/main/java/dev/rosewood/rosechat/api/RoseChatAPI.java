package dev.rosewood.rosechat.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderSettingManager;
import dev.rosewood.rosechat.message.MessageSender;
import dev.rosewood.rosechat.message.MessageWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The API for the RoseChat plugin.
 */
public class RoseChatAPI {

    private static RoseChatAPI instance;
    private final RoseChat plugin;
    private final LocaleManager localeManager;
    private final DataManager dataManager;
    private final GroupManager groupManager;

    private RoseChatAPI() {
        this.plugin = RoseChat.getInstance();
        this.localeManager = this.plugin.getManager(LocaleManager.class);
        this.dataManager = this.plugin.getManager(DataManager.class);
        this.groupManager = this.plugin.getManager(GroupManager.class);
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
     * Parses a string into the RoseChat MessageWrapper and Tokenizer, allowing for hex color, tags and emoji in other text.
     * @param string The string to parse.
     * @return A BaseComponent[] consisting of the parsed message.
     */
    public BaseComponent[] parse(String string) {
        return new MessageWrapper(new MessageSender(null, null), string, "other").build();
    }

    /**
     * Creates a new chat channel.
     * @param id The ID to use.
     * @param format The format to use.
     * @return The new chat channel.
     */
    public ChatChannel createChannel(String id, String format) {
        ChatChannel channel = new ChatChannel(id, format);
        this.plugin.getManager(ChannelManager.class).addChannel(channel);
        return channel;
    }

    /**
     * Deletes a chat channel.
     * @param channel The channel to delete.
     */
    public void deleteChannel(ChatChannel channel) {
        this.plugin.getManager(ChannelManager.class).removeChannel(channel);
    }

    /**
     * @param id The ID to use.
     * @return The channel found, or null if it doesn't exist.
     */
    public ChatChannel getChannelById(String id) {
        return this.plugin.getManager(ChannelManager.class).getChannel(id);
    }

    /**
     * @return A list of all the chat channels.
     */
    public List<ChatChannel> getChannels() {
        return new ArrayList<>(this.plugin.getManager(ChannelManager.class).getChannels().values());
    }

    /**
     * @return A list of all the chat channel IDs.
     */
    public List<String> getChannelIDs() {
        return new ArrayList<>(this.plugin.getManager(ChannelManager.class).getChannels().keySet());
    }

    /**
     * Creates a new chat replacement.
     * @param id The ID to use.
     * @param text The text that will be replaced.
     * @param replacement The text that will replace.
     * @param hoverText The text shown when the replacement is hovered over.
     * @return The new chat replacement.
     */
    public ChatReplacement createReplacement(String id, String text, String replacement, String hoverText) {
        ChatReplacement chatReplacement = new ChatReplacement(id, text, replacement, hoverText);
        this.plugin.getManager(PlaceholderSettingManager.class).addReplacement(chatReplacement);
        return chatReplacement;
    }

    /**
     * Deletes a chat replacement.
     * @param replacement The replacement to delete.
     */
    public void deleteReplacement(ChatReplacement replacement) {
        this.plugin.getManager(PlaceholderSettingManager.class).removeReplacement(replacement);
    }

    /**
     * @param id The ID to use.
     * @return The chat replacement found, or null if it doesn't exist.
     */
    public ChatReplacement getReplacementById(String id) {
        return this.plugin.getManager(PlaceholderSettingManager.class).getReplacement(id);
    }

    /**
     * @return A list of all chat replacements.
     */
    public List<ChatReplacement> getReplacements() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getReplacements().values());
    }

    /**
     * @return A list of all chat replacement IDs.
     */
    public List<String> getReplacementIDs() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getReplacements().keySet());
    }

    /**
     * Creates a new emoji.
     * @param id The ID to use.
     * @param text The text to be replaced.
     * @param replacement The text that will replace.
     * @param hoverText The text shown when the replacement is hovered over.
     * @param font The font to use for the emoji.
     * @return The new emoji.
     */
    public ChatReplacement createEmoji(String id, String text, String replacement, String hoverText, String font) {
        ChatReplacement chatReplacement = new ChatReplacement(id, text, replacement, hoverText, font);
        this.plugin.getManager(PlaceholderSettingManager.class).addEmoji(chatReplacement);
        return chatReplacement;
    }

    /**
     * Deletes an emoji.
     * @param emoji The emoji to delete.
     */
    public void deleteEmoji(ChatReplacement emoji) {
        this.plugin.getManager(PlaceholderSettingManager.class).removeEmoji(emoji);
    }

    /**
     * @param id The ID to use.
     * @return The emoji found, or null if it doesn't exist.
     */
    public ChatReplacement getEmojiById(String id) {
        return this.plugin.getManager(PlaceholderSettingManager.class).getEmoji(id);
    }

    /**
     * @return A list of all emojis, specified in emoji.yml.
     */
    public List<ChatReplacement> getEmojis() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getEmojis().values());
    }

    /**
     * @return A list of all emoji IDs.
     */
    public List<String> getEmojiIDs() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getEmojis().keySet());
    }

    /**
     * Creates a new group chat.
     * @param owner The owner of the group chat.
     * @return The new group chat.
     */
    public GroupChat createGroupChat(UUID owner) {
        GroupChat groupChat = new GroupChat(owner);
        groupChat.addMember(owner);
        this.groupManager.addGroupChat(groupChat);
        return groupChat;
    }

    /**
     * Deletes a group chat.
     * @param groupChat The group chat to delete.
     */
    public void deleteGroupChat(GroupChat groupChat) {
        this.groupManager.removeGroupChat(groupChat);
    }

    /**
     * @param owner The owner to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChat getGroupChat(UUID owner) {
        return this.groupManager.getGroupChat(owner);
    }

    /**
     * @return A list of all group chats.
     */
    public List<GroupChat> getGroupChats() {
        return new ArrayList<>(this.groupManager.getGroupChats().values());
    }

    /**
     * @return A list of all group chat IDs.
     */
    public List<UUID> getGroupChatIDs() {
        return new ArrayList<>(this.groupManager.getGroupChats().keySet());
    }

    /**
     * Creates a new tag.
     * @param id The ID to use.
     * @return The new tag.
     */
    public Tag createTag(String id) {
        Tag tag = new Tag(id);
        this.plugin.getManager(PlaceholderSettingManager.class).addTag(tag);
        return tag;
    }

    /**
     * Deletes a tag.
     * @param tag The tag to delete
     */
    public void deleteTag(Tag tag) {
        this.plugin.getManager(PlaceholderSettingManager.class).removeTag(tag);
    }

    /**
     * @param id The tag to use.
     * @return The tag found, or null if it doesn't exist.
     */
    public Tag getTagById(String id) {
        return this.plugin.getManager(PlaceholderSettingManager.class).getTag(id);
    }

    /**
     * @return A list of all tags.
     */
    public List<Tag> getTags() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getTags().values());
    }

    /**
     * @return A list of all tag IDs.
     */
    public List<String> getTagIDs() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getTags().keySet());
    }

    /**
     * @param player The player to get the data of.
     * @return The data of the player.
     */
    public PlayerData getPlayerData(UUID player) {
        return this.dataManager.getPlayerData(player);
    }

    /**
     * @return An instance of the locale manager.
     */
    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }

    /**
     * @return An instance of the group manager.
     */
    public GroupManager getGroupManager() {
        return this.groupManager;
    }
}
