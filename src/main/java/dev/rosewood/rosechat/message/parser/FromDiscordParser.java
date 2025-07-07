package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public class FromDiscordParser implements MessageParser {

    public static final MessageParser INSTANCE = new FromDiscordParser();

    private FromDiscordParser() {

    }

    @Override
    public MessageTokenizerResults parse(RoseMessage message, RosePlayer viewer, String format) {
        if (Settings.USE_MARKDOWN_FORMATTING.get()) {
            return MessageTokenizer.tokenize(message, viewer, format, MessageDirection.DISCORD_TO_MINECRAFT,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.FROM_DISCORD_BUNDLE,
                    Tokenizers.MARKDOWN_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.DEFAULT_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, viewer, format, MessageDirection.DISCORD_TO_MINECRAFT,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.FROM_DISCORD_BUNDLE,
                    Tokenizers.DEFAULT_BUNDLE);
        }
    }

}
