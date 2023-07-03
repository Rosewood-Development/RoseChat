package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public interface MessageParser {

    /**
     * Parses the message using the tokenizer.
     * @param message The {@link RoseMessage} to be parsed.
     * @param sender The {@link RosePlayer} who is sending the message.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param format The format to parse into.
     * @return A {@link BaseComponent[]} containing the parsed message.
     */
    BaseComponent[] parse(RoseMessage message, RosePlayer sender, RosePlayer viewer, String format);

    /**
     * @return The direction of the parsed message.
     */
    MessageDirection getMessageDirection();

}
