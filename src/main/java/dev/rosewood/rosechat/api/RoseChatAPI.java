package dev.rosewood.rosechat.api;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.manager.BungeeManager;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.manager.EmojiManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.manager.ReplacementManager;
import dev.rosewood.rosechat.manager.TagManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
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
     * Parses a string into the RoseChat {@link RoseMessage} and {@link MessageTokenizer}, allowing for hex color, tags and emoji in other text.
     * @param sender The person sending the message.
     * @param viewer The person receiving the message.
     * @param message The string to parse.
     * @param placeholders A set of {@link StringPlaceholders} to be parsed in the message.
     * @return A {@link BaseComponent} consisting of the parsed message.
     */
    public BaseComponent[] parse(RosePlayer sender, RosePlayer viewer, String message, StringPlaceholders placeholders) {
        RoseMessage roseMessage = new RoseMessage(sender, MessageLocation.NONE, message);
        roseMessage.setPlaceholders(placeholders);

        return roseMessage.parse(viewer, null);
    }

    /**
     * Parses a string into the RoseChat {@link RoseMessage} and {@link MessageTokenizer}, allowing for hex color, tags and emoji in other text.
     * @param sender The person sending the message.
     * @param viewer The person receiving the message.
     * @param message The string to parse.
     * @return A {@link BaseComponent} consisting of the parsed message.
     */
    public BaseComponent[] parse(RosePlayer sender, RosePlayer viewer, String message) {
        return new RoseMessage(sender, MessageLocation.NONE, message).parse(viewer, null);
    }

    /**
     * Parses a string into the RoseChat {@link RoseMessage} and {@link MessageTokenizer}, allowing for hex color, tags and emoji in other text.
     * @param sender The person sending the message.
     * @param viewer The person receiving the message.
     * @param message The string to parse.
     * @param location The location that the chat message is in.
     * @return A {@link BaseComponent} consisting of the parsed message.
     */
    public BaseComponent[] parse(RosePlayer sender, RosePlayer viewer, String message, MessageLocation location) {
        return new RoseMessage(sender, location, message).parse(viewer, null);
    }

    public void sendToChannel(Player player, String message, Channel channel, boolean checkPermissions) {
        RosePlayer sender = new RosePlayer(player);
        PlayerData data = sender.getPlayerData();

        if (checkPermissions) {
            // Don't send the message if the player doesn't have permission.
            if (data == null || !sender.hasPermission("rosechat.chat")) {
                this.getLocaleManager().sendComponentMessage(player, "no-permission");
                return;
            }

            // Check the mute expiry.
            if (data.isMuteExpired()) {
                data.unmute();
                data.save();
            }

            // Check if the player is muted.
            if (data.isMuted() && !sender.hasPermission("rosechat.mute.bypass")) {
                sender.sendLocaleMessage("command-mute-cannot-send");
                return;
            }

            // Check if the player has permission for this channel.
            if (!sender.hasPermission("rosechat.channel." + channel.getId())) {
                sender.sendLocaleMessage("no-permission");
                return;
            }
        }

        // Make the message isn't empty.
        if (MessageUtils.isMessageEmpty(message)) {
            sender.sendLocaleMessage("message-blank");
            return;
        }

        if (channel.isMuted() && !sender.hasPermission("rosechat.mute.bypass")) {
            sender.sendLocaleMessage("channel-muted");
            return;
        }

        // Send the message.
        channel.send(sender, message);

        // Update the player's display name if the setting is enabled.
        if (Setting.UPDATE_DISPLAY_NAMES.getBoolean() && data.getNickname() != null && !sender.getDisplayName().equals(data.getNickname())) {
            RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
                RoseMessage roseMessage = new RoseMessage(sender, MessageLocation.NICKNAME, data.getNickname());
                roseMessage.parse(sender, null);

                if (data.getNickname() != null) NicknameCommand.setDisplayName(sender, roseMessage);
            });
        }
    }

    /**
     * Creates a new chat channel.
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

    public Channel getDefaultChannel() {
        return this.getChannelManager().getDefaultChannel();
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
    public GroupChannel createGroupChat(String id, Player owner) {
        GroupChannel groupChat = new GroupChannel(id);
        groupChat.setOwner(owner.getUniqueId());
        groupChat.onJoin(owner);

        this.getGroupManager().addGroupChat(groupChat);
        this.getGroupManager().addMember(groupChat, owner.getUniqueId());

        return groupChat;
    }

    /**
     * Deletes a group chat.
     * @param groupChat The group chat to delete.
     */
    public void deleteGroupChat(GroupChannel groupChat) {
        this.getGroupManager().removeGroupChat(groupChat);
        this.getGroupManager().deleteGroupChat(groupChat);
    }

    /**
     * Adds a player to a group chat.
     * @param groupChat The group chat to be added to.
     * @param member The player to be added.
     */
    public void addGroupChatMember(GroupChannel groupChat, Player member) {
        groupChat.onJoin(member);
        this.getGroupManager().addMember(groupChat, member.getUniqueId());
    }

    /**
     * Removes a player from a group chat.
     * @param groupChat The group chat to be removed from.
     * @param member The player to be removed.
     */
    public void removeGroupChatMember(GroupChannel groupChat, Player member) {
        groupChat.onLeave(member);
        this.getGroupManager().removeMember(groupChat, member.getUniqueId());
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
