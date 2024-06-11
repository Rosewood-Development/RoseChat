package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import net.md_5.bungee.api.chat.BaseComponent;

public class FromDiscordParser implements MessageParser<BaseComponent[]> {

    @Override
    public MessageTokenizerResults<BaseComponent[]> parse(RoseMessage message, RosePlayer viewer, String format) {
        if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
            return MessageTokenizer.tokenize(message, viewer, format,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.FROM_DISCORD_BUNDLE,
                    Tokenizers.MARKDOWN_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.DEFAULT_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, viewer, format,
                    Tokenizers.DISCORD_EMOJI_BUNDLE,
                    Tokenizers.FROM_DISCORD_BUNDLE,
                    Tokenizers.DEFAULT_BUNDLE);
        }
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.DISCORD_TO_MINECRAFT;
    }

}
