package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;

public class ToDiscordSpoilerToken extends Token {

    private final MessageWrapper messageWrapper;
    private final String replacement;

    public ToDiscordSpoilerToken(MessageWrapper messageWrapper, RoseSender sender, RoseSender viewer, String originalContent, String replacement) {
        super(sender, viewer, originalContent);
        this.messageWrapper = messageWrapper;
        this.replacement = replacement;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        componentBuilder.append("|").append("|");

        BaseComponent[] components = new MessageTokenizer.Builder()
                .sender(this.getSender()).viewer(this.getViewer()).location(MessageLocation.OTHER)
                .message(this.messageWrapper)
                .tokenizers(Tokenizers.TAG_TOKENIZERS)
                .tokenize(this.replacement).toComponents();

        componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);

        componentBuilder.append("|").append("|");
        return componentBuilder.create();
    }
}
