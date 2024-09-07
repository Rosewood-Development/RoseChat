package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import java.util.List;
import java.util.UUID;

public interface DiscordChatProvider {

    /**
     * Sends a message to Discord.
     * @param roseMessage The message to send.
     * @param group The group that the message was sent from.
     * @param channel The channel that the message should go in.
     */
    void sendMessage(RoseMessage roseMessage, Channel group, String channel);

    /**
     * Deletes a message from Discord.
     * @param id The ID of the message.
     */
    void deleteMessage(String id);

    /**
     * @param id The ID of the channel.
     * @return The name of the channel.
     */
    String getChannelName(String id);

    /**
     * @param name The name of the channel.
     * @return The channel as a mention.
     */
    String getChannelFromName(String name);

    /**
     * @return The ID of the Discord server.
     */
    String getServerId();

    /**
     * @param id The ID of the user.
     * @return The name of the user.
     */
    String getUserFromId(String id);

    /**
     *
     * @param id The ID of the user.
     * @return The {@link UUID} of the player.
     */
    UUID getUUIDFromId(String id);

    /**
     * @param id The ID of the role.
     * @return The name of the role.
     */
    String getRoleFromId(String id);

    /**
     * @param id The ID of the role.
     * @return The UUIDs of the players who have this role.
     */
    List<UUID> getPlayersWithRole(String id);

    /**
     * @param name The name of the emoji to grab.
     * @return The mention of the emoji.
     */
    String getCustomEmoji(String name);

    /**
     * @param name The name of the user to tag.
     * @return The tag.
     */
    String getUserTag(String name);

    /**
     * @param input The string to match from.
     * @return The name of the member.
     */
    DetectedMention matchPartialMember(String input);

    /**
     * @param input The string to match from.
     * @return The name of the channel.
     */
    DetectedMention matchPartialChannel(String input);

    record DetectedMention(String name, String mention, int consumedTextLength) { }

}
