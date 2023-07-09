package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeParser implements MessageParser {

    @Override
    public BaseComponent[] parse(RoseMessage message, RosePlayer sender, RosePlayer viewer, String format) {
        // If the message is a private message, then the viewer should be the one who received the pm.
        RosePlayer receiver;

        if (message.getMessageRules() != null) {
            receiver = message.getMessageRules().isPrivateMessage() ?
                    message.getMessageRules().getPrivateMessageInfo().getReceiver() : viewer;
        } else {
            receiver = viewer;
        }

        if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
            return MessageTokenizer.tokenize(message, receiver, message.getMessage(),
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.MARKDOWN_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.BUNGEE_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, receiver, message.getMessage(),
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.BUNGEE_BUNDLE);
        }
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.FROM_BUNGEE_SERVER;
    }

}
