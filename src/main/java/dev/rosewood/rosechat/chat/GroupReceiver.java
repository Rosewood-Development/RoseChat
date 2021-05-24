package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.message.MessageWrapper;
import java.util.List;
import java.util.UUID;

public interface GroupReceiver {

    /**
     * Sends the message wrapper to the entire group.
     * @param messageWrapper The message wrapper to send.
     */
    void send(MessageWrapper messageWrapper);

    /**
     * @return The members.
     */
    List<UUID> getMembers();
}
