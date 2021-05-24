package dev.rosewood.rosechat.events;

import dev.rosewood.rosechat.message.MessageWrapper;

public class PreParseMessageEvent extends MessageEvent {

    public PreParseMessageEvent(MessageWrapper message) {
        super(message);
    }
}
