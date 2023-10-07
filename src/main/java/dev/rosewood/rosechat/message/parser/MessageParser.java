package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;

public interface MessageParser<T> {

    /**
     * Parses the message using the tokenizer.
     * @param message The {@link RoseMessage} to be parsed.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param format The format to parse into.
     * @return A {@link MessageTokenizerResults} containing the parsed message.
     */
    MessageTokenizerResults<T> parse(RoseMessage message, RosePlayer viewer, String format);

    /**
     * @return The direction of the parsed message.
     */
    MessageDirection getMessageDirection();

}
