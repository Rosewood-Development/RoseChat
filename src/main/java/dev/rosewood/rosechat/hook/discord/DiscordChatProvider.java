package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageWrapper;
import java.util.List;
import java.util.UUID;

public interface DiscordChatProvider {

    /**
     * Sends a message to Discord.
     * @param messageWrapper The message to send.
     * @param group The group that the message was sent from.
     * @param channel The channel that the message should go in.
     */
    void sendMessage(MessageWrapper messageWrapper, Group group, String channel);

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
     * @return The ID of the Discord server.
     */
    String getServerId();

    /**
     * @param id The ID of the user.
     * @return The name of the user.
     */
    String getUserFromId(String id);

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
}
