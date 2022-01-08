package dev.rosewood.rosechat.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.GroupChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.manager.EmojiManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.manager.ReplacementManager;
import dev.rosewood.rosechat.manager.TagManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
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
public class RoseChatAPI {

    private static RoseChatAPI instance;
    private final RoseChat plugin;
    private final LocaleManager localeManager;
    private final DataManager dataManager;
    private final GroupManager groupManager;
    private final ChannelManager channelManager;
    private final PlaceholderManager placeholderManager;
    private final EmojiManager emojiManager;
    private final ReplacementManager replacementManager;
    private final TagManager tagManager;
    private Class<?> spigotConfigClass;
    private Field bungeeField;

    private RoseChatAPI() {
        this.plugin = RoseChat.getInstance();
        this.localeManager = this.plugin.getManager(LocaleManager.class);
        this.dataManager = this.plugin.getManager(DataManager.class);
        this.groupManager = this.plugin.getManager(GroupManager.class);
        this.channelManager = this.plugin.getManager(ChannelManager.class);
        this.placeholderManager = this.plugin.getManager(PlaceholderManager.class);
        this.emojiManager = this.plugin.getManager(EmojiManager.class);
        this.replacementManager = this.plugin.getManager(ReplacementManager.class);
        this.tagManager = this.plugin.getManager(TagManager.class);
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
        return new MessageWrapper(sender, MessageLocation.OTHER, null, message).parse(null, viewer);
    }

    /**
     * Creates a new chat channel.
     * @param id The ID to use.
     * @param format The format to use.
     * @return The new chat channel.
     */
    public ChatChannel createChannel(String id, String format) {
        ChatChannel channel = new ChatChannel(id, format);
        this.channelManager.addChannel(channel);
        return channel;
    }

    /**
     * Deletes a chat channel.
     * @param channel The channel to delete.
     */
    public void deleteChannel(ChatChannel channel) {
        this.channelManager.removeChannel(channel);
    }

    /**
     * @param id The ID to use.
     * @return The channel found, or null if it doesn't exist.
     */
    public ChatChannel getChannelById(String id) {
        return this.channelManager.getChannel(id);
    }

    /**
     * @return A list of all the chat channels.
     */
    public List<ChatChannel> getChannels() {
        return new ArrayList<>(this.channelManager.getChannels().values());
    }

    /**
     * @return A list of all the chat channel IDs.
     */
    public List<String> getChannelIDs() {
        return new ArrayList<>(this.channelManager.getChannels().keySet());
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
        this.replacementManager.addReplacement(chatReplacement);
        return chatReplacement;
    }

    /**
     * Deletes a chat replacement.
     * @param replacement The replacement to delete.
     */
    public void deleteReplacement(ChatReplacement replacement) {
        this.replacementManager.removeReplacement(replacement);
    }

    /**
     * @param id The ID to use.
     * @return The chat replacement found, or null if it doesn't exist.
     */
    public ChatReplacement getReplacementById(String id) {
        return this.replacementManager.getReplacement(id);
    }

    /**
     * @return A list of all chat replacements.
     */
    public List<ChatReplacement> getReplacements() {
        return new ArrayList<>(this.replacementManager.getReplacements().values());
    }

    /**
     * @return A list of all chat replacement IDs.
     */
    public List<String> getReplacementIDs() {
        return new ArrayList<>(this.replacementManager.getReplacements().keySet());
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
        this.emojiManager.addEmoji(chatReplacement);
        return chatReplacement;
    }

    /**
     * Deletes an emoji.
     * @param emoji The emoji to delete.
     */
    public void deleteEmoji(ChatReplacement emoji) {
        this.emojiManager.removeEmoji(emoji);
    }

    /**
     * @param id The ID to use.
     * @return The emoji found, or null if it doesn't exist.
     */
    public ChatReplacement getEmojiById(String id) {
        return this.emojiManager.getEmoji(id);
    }

    /**
     * @return A list of all emojis, specified in emojis.yml.
     */
    public List<ChatReplacement> getEmojis() {
        return new ArrayList<>(this.emojiManager.getEmojis().values());
    }

    /**
     * @return A list of all emoji IDs.
     */
    public List<String> getEmojiIds() {
        return new ArrayList<>(this.emojiManager.getEmojis().keySet());
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
        this.groupManager.addGroupChat(groupChat);
        this.groupManager.addMember(groupChat, owner);
        return groupChat;
    }

    /**
     * Deletes a group chat.
     * @param groupChat The group chat to delete.
     */
    public void deleteGroupChat(GroupChat groupChat) {
        this.groupManager.removeGroupChat(groupChat);
        this.groupManager.deleteGroupChat(groupChat);
    }

    /**
     * Adds a player to a group chat.
     * @param groupChat The group chat to be added to.
     * @param member The player to be added.
     */
    public void addGroupChatMember(GroupChat groupChat, Player member) {
        groupChat.addMember(member);
        this.groupManager.addMember(groupChat, member.getUniqueId());
    }

    /**
     * Removes a player from a group chat.
     * @param groupChat The group chat to be removed from.
     * @param member The player to be removed.
     */
    public void removeGroupChatMember(GroupChat groupChat, Player member) {
        groupChat.removeMember(member);
        this.groupManager.removeMember(groupChat, member.getUniqueId());
    }

    /**
     * @param owner The owner to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChat getGroupChatByOwner(UUID owner) {
        return this.groupManager.getGroupChatByOwner(owner);
    }

    /**
     * @param id The ID to use.
     * @return The group chat found, or null if it doesn't exist.
     */
    public GroupChat getGroupChatById(String id) {
        return this.groupManager.getGroupChatById(id);
    }

    /**
     * @return A list of all group chats.
     */
    public List<GroupChat> getGroupChats() {
        return new ArrayList<>(this.groupManager.getGroupChats().values());
    }

    /**
     * @param player The UUID of the player to use.
     * @return A list of all group chats that the player is in.
     */
    public List<GroupChat> getGroupChats(UUID player) {
        return this.groupManager.getGroupChats().values().stream().filter(gc -> gc.getMembers().contains(player)).collect(Collectors.toList());
    }

    /**
     * @return A list of all group chat IDs.
     */
    public List<String> getGroupChatIDs() {
        return new ArrayList<>(this.groupManager.getGroupChats().keySet());
    }

    /**
     * Creates a new tag.
     * @param id The ID to use.
     * @return The new tag.
     */
    public Tag createTag(String id) {
        Tag tag = new Tag(id);
        this.tagManager.addTag(tag);
        return tag;
    }

    /**
     * Deletes a tag.
     * @param tag The tag to delete
     */
    public void deleteTag(Tag tag) {
        this.tagManager.removeTag(tag);
    }

    /**
     * @param id The id to use.
     * @return The tag found, or null if it doesn't exist.
     */
    public Tag getTagById(String id) {
        return this.tagManager.getTag(id);
    }

    /**
     * @return A list of all tags.
     */
    public List<Tag> getTags() {
        return new ArrayList<>(this.tagManager.getTags().values());
    }

    /**
     * @return A list of all tag IDs.
     */
    public List<String> getTagIDs() {
        return new ArrayList<>(this.tagManager.getTags().keySet());
    }

    /**
     * @param uuid The uuid of the player whose data should be got.
     * @return The data of the player.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return this.dataManager.getPlayerData(uuid);
    }

    /**
     * @return An instance of the locale manager.
     */
    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }

    /**
     * @return An instance of the data manager.
     */
    public DataManager getDataManager() {
        return this.dataManager;
    }

    /**
     * @return An instance of the group manager.
     */
    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    /**
     * @return An instance of the channel manager.
     */
    public ChannelManager getChannelManager() {
        return this.channelManager;
    }

    /**
     * @return An instance of the placeholder manager.
     */
    public PlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }

    /**
     * @return An instance of the emoji manager.
     */
    public EmojiManager getEmojiManager() {
        return this.emojiManager;
    }

    /**
     * @return An instance of the replacement manager.
     */
    public ReplacementManager getReplacementManager() {
        return this.replacementManager;
    }

    /**
     * @return An instance of the tag manager.
     */
    public TagManager getTagManager() {
        return this.tagManager;
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
