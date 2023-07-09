package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class ToDiscordParser implements MessageParser {

    @Override
    public BaseComponent[] parse(RoseMessage message, RosePlayer sender, RosePlayer viewer, String format) {
        if (ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean()) {
            return MessageTokenizer.tokenize(message, viewer, format,
                    Tokenizers.TO_DISCORD_BUNDLE,
                    Tokenizers.DISCORD_FORMATTING_BUNDLE,
                    Tokenizers.DEFAULT_DISCORD_BUNDLE);
        } else {
            return MessageTokenizer.tokenize(message, viewer, format,
                    Tokenizers.TO_DISCORD_BUNDLE,
                    Tokenizers.DEFAULT_DISCORD_BUNDLE);
        }
    }

    @Override
    public MessageDirection getMessageDirection() {
        return MessageDirection.TO_DISCORD;
    }

}
