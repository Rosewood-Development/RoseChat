package dev.rosewood.rosechat.hook.discord;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageWrapper;

public interface DiscordChatProvider {

    void sendMessage(MessageWrapper messageWrapper, Group group, String channel);

}
