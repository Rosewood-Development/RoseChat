package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.quote;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import net.md_5.bungee.api.chat.BaseComponent;

public class DiscordQuoteToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final String replacement;

    public DiscordQuoteToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, String originalContent, String replacement) {
        super(sender, viewer, originalContent);
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.replacement = replacement;
    }

    @Override
    public BaseComponent[] toComponents() {
        String format = Setting.DISCORD_FORMAT_BLOCK_QUOTES.getString();
        String quote = format.contains("%message%") ? format.replace("%message%", this.replacement) : format + this.replacement;
        return new MessageTokenizer.Builder()
                .message(this.messageWrapper).group(this.group).sender(this.getSender())
                .viewer(this.getViewer()).location(this.messageWrapper.getLocation())
                .tokenizers(Tokenizers.BLOCK_QUOTES_TOKENIZERS)
                .tokenize(quote).toComponents();
    }
}
