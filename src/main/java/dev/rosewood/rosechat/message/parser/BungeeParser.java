package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import net.md_5.bungee.api.chat.BaseComponent;

public class BungeeParser implements MessageParser<BaseComponent[]> {

    @Override
    public MessageTokenizerResults<BaseComponent[]> parse(RoseMessage message, RosePlayer viewer, String format) {
        if (Settings.USE_MARKDOWN_FORMATTING.get()) {
            return MessageTokenizer.tokenize(message, viewer, format, MessageDirection.SERVER_TO_SERVER,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.MARKDOWN_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.BUNGEE_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, viewer, format, MessageDirection.SERVER_TO_SERVER,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.BUNGEE_BUNDLE);
        }
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.SERVER_TO_SERVER;
    }

}
