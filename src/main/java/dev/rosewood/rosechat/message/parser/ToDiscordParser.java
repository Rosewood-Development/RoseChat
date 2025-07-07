package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;

public class ToDiscordParser implements MessageParser {

    public static final MessageParser INSTANCE = new ToDiscordParser();

    private ToDiscordParser() {

    }

    @Override
    public MessageTokenizerResults parse(RoseMessage message, RosePlayer viewer, String format) {
        if (Settings.USE_MARKDOWN_FORMATTING.get()) {
            return MessageTokenizer.tokenize(message, viewer, format, MessageDirection.MINECRAFT_TO_DISCORD,
                    Tokenizers.TO_DISCORD_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.DEFAULT_DISCORD_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, viewer, format, MessageDirection.MINECRAFT_TO_DISCORD,
                    Tokenizers.TO_DISCORD_BUNDLE,
                    Tokenizers.DEFAULT_DISCORD_BUNDLE);
        }
    }

}
