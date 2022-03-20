package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import net.md_5.bungee.api.chat.BaseComponent;

public class DiscordSpoilerToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final String replacement;

    public DiscordSpoilerToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, String originalContent, String replacement) {
        super(sender, viewer, originalContent);
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.replacement = replacement;
    }

    @Override
    public BaseComponent[] toComponents() {
        String format = Setting.DISCORD_FORMAT_SPOILER.getString();
        String spoiler = format.contains("%message%") ? format.replace("%message%", this.replacement) : format + this.replacement;
        return new MessageTokenizer.Builder()
                .message(this.messageWrapper).group(this.group).sender(this.getSender())
                .viewer(getViewer()).location(this.messageWrapper.getLocation())
                .tokenizers(Tokenizers.DEFAULT_WITH_DISCORD_TOKENIZERS)
                .tokenize(spoiler).toComponents();
    }
}
