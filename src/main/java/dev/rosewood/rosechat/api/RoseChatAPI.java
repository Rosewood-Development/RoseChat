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

public class RoseChatAPI {

    private static RoseChatAPI instance;
    private final RoseChat plugin;
    private final LocaleManager localeManager;

    private RoseChatAPI() {
        this.plugin = RoseChat.getInstance();
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    public static RoseChatAPI getInstance() {
        if (instance == null) instance = new RoseChatAPI();
        return instance;
    }

    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public BaseComponent[] parse(String string) {
        return new MessageWrapper(new MessageSender(null, null), string, "other").build();
    }

    public ChatChannel createChannel(String id, String format) {
        ChatChannel channel = new ChatChannel(id, format);
        this.plugin.getManager(ChannelManager.class).addChannel(channel);
        return channel;
    }

    public void deleteChannel(ChatChannel channel) {
        this.plugin.getManager(ChannelManager.class).removeChannel(channel);
    }

    public ChatChannel getChannelById(String id) {
        return this.plugin.getManager(ChannelManager.class).getChannel(id);
    }

    public List<ChatChannel> getChannels() {
        return new ArrayList<>(this.plugin.getManager(ChannelManager.class).getChannels().values());
    }

    public List<String> getChannelIDs() {
        return new ArrayList<>(this.plugin.getManager(ChannelManager.class).getChannels().keySet());
    }

    public ChatReplacement createReplacement(String id, String text, String replacement, String hoverText) {
        ChatReplacement chatReplacement = new ChatReplacement(id, text, replacement, hoverText);
        this.plugin.getManager(PlaceholderSettingManager.class).addReplacement(chatReplacement);
        return chatReplacement;
    }

    public void deleteReplacement(ChatReplacement replacement) {
        this.plugin.getManager(PlaceholderSettingManager.class).removeReplacement(replacement);
    }

    public ChatReplacement getReplacementById(String id) {
        return this.plugin.getManager(PlaceholderSettingManager.class).getReplacement(id);
    }

    public List<ChatReplacement> getReplacements() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getReplacements().values());
    }

    public List<String> getReplacementIDs() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getReplacements().keySet());
    }

    public ChatReplacement createEmoji(String id, String text, String replacement, String hoverText, String font) {
        ChatReplacement chatReplacement = new ChatReplacement(id, text, replacement, hoverText, font);
        this.plugin.getManager(PlaceholderSettingManager.class).addEmoji(chatReplacement);
        return chatReplacement;
    }

    public void deleteEmoji(ChatReplacement replacement) {
        this.plugin.getManager(PlaceholderSettingManager.class).removeEmoji(replacement);
    }

    public ChatReplacement getEmojiById(String id) {
        return this.plugin.getManager(PlaceholderSettingManager.class).getEmoji(id);
    }

    public List<ChatReplacement> getEmojis() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getEmojis().values());
    }

    public List<String> getEmojiIDs() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getEmojis().keySet());
    }

    public GroupChat createGroupChat(UUID owner) {
        GroupChat groupChat = new GroupChat(UUID.randomUUID());
        groupChat.setOwner(owner);
        this.plugin.getManager(GroupManager.class).addGroupChat(groupChat);
        return groupChat;
    }

    public void deleteGroupChat(GroupChat groupChat) {
        this.plugin.getManager(GroupManager.class).removeGroupChat(groupChat);
    }

    public GroupChat getGroupChatByOwner(UUID owner) {
        return this.plugin.getManager(GroupManager.class).getGroupChatByOwner(owner);
    }

    public List<GroupChat> getGroupChats() {
        return new ArrayList<>(this.plugin.getManager(GroupManager.class).getGroupChats().values());
    }

    public List<String> getGroupChatIDs() {
        return new ArrayList<>(this.plugin.getManager(GroupManager.class).getGroupChats().keySet());
    }

    public Tag createTag(String id) {
        Tag tag = new Tag(id);
        this.plugin.getManager(PlaceholderSettingManager.class).addTag(tag);
        return tag;
    }

    public void deleteTag(Tag tag) {
        this.plugin.getManager(PlaceholderSettingManager.class).removeTag(tag);
    }

    public Tag getTagById(String id) {
        return this.plugin.getManager(PlaceholderSettingManager.class).getTag(id);
    }

    public List<Tag> getTags() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getTags().values());
    }

    public List<String> getTagIDs() {
        return new ArrayList<>(this.plugin.getManager(PlaceholderSettingManager.class).getTags().keySet());
    }

    public PlayerData getPlayerData(UUID player) {
        return this.plugin.getManager(DataManager.class).getPlayerData(player);
    }

    public LocaleManager getLocaleManager() {
        return this.localeManager;
    }
}
