package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public interface MessageParser {

    /**
     * Parses the message using the tokenizer.
     * @param message The {@link RoseMessage} to be parsed.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param format The format to parse into.
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    MessageTokenizerResults parse(RoseMessage message, RosePlayer viewer, String format);

    static MessageParser roseChat() {
        return RoseChatParser.INSTANCE;
    }

    static MessageParser bungeeProxy() {
        return BungeeProxyParser.INSTANCE;
    }

    static MessageParser fromDiscord() {
        return FromDiscordParser.INSTANCE;
    }

    static MessageParser toDiscord() {
        return ToDiscordParser.INSTANCE;
    }

}
