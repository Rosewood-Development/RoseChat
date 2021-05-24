package dev.rosewood.rosechat.events;

import dev.rosewood.rosechat.message.MessageWrapper;

public class PostParseMessageEvent extends MessageEvent {

    public PostParseMessageEvent(MessageWrapper message) {
        super(message);
    }
}
