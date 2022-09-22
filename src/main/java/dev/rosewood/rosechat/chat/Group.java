package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import java.util.List;
import java.util.UUID;

public interface Group {

    /**
     * Sends the message wrapper to the group.
     * @param message The message wrapper to send.
     */
    void send(MessageWrapper message);

    /**
     * Sends a json message to the group.
     * @param sender The name of the player who sent the message.
     * @param rawMessage The message that was originally sent.
     */
    void sendJson(RoseSender sender, String rawMessage);

    /**
     * Sends a message from discord.
     * @param message The message that was sent.
     */
    void sendFromDiscord(String id, MessageWrapper message);

    /**
     * @return The members of the group.
     */
    List<UUID> getMembers();

    /**
     * @return A string used in the permissions for this group.
     */
    String getLocationPermission();

}
