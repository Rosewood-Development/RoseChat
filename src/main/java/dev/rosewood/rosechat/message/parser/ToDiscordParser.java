package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;

public class ToDiscordParser implements MessageParser<String> {

    @Override
    public MessageTokenizerResults<String> parse(RoseMessage message, RosePlayer viewer, String format) {
        if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
            return MessageTokenizer.tokenize(message, viewer, format, TokenComposer.markdown(),
                    Tokenizers.TO_DISCORD_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.DEFAULT_DISCORD_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, viewer, format, TokenComposer.markdown(),
                    Tokenizers.TO_DISCORD_BUNDLE,
                    Tokenizers.DEFAULT_DISCORD_BUNDLE);
        }
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.MINECRAFT_TO_DISCORD;
    }

}
