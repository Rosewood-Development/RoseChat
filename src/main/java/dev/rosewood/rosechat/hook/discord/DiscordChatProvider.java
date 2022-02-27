package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageWrapper;

public interface DiscordChatProvider {

    void sendMessage(MessageWrapper messageWrapper, Group group, String channel);

    void deleteMessage(String id);

    String getChannelName(String id);

    String getServerId();

    String getUserFromId(String id);

    String getRoleFromId(String id);
}
