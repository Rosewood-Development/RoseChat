package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.format;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordFormattingToken extends Token {

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final CustomPlaceholder placeholder;

    public DiscordFormattingToken(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, CustomPlaceholder placeholder) {
        super(sender, viewer, "{" + placeholder.getId() + "}");
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.placeholder = placeholder;
    }

    @Override
    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        BaseComponent[] components = MessageUtils.parseCustomPlaceholder(this.getSender(), this.getSender(), this.placeholder.getId(), MessageTokenizer.TO_DISCORD_TOKENIZERS,
                MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group, this.messageWrapper.getPlaceholders()).build());

        if (components != null) componentBuilder.append(components);

        return componentBuilder.create();
    }

    @Override
    public String asString() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        BaseComponent[] components = MessageUtils.parseCustomPlaceholder(this.getSender(), this.getSender(), this.placeholder.getId(), MessageTokenizer.TO_DISCORD_TOKENIZERS,
                MessageUtils.getSenderViewerPlaceholders(this.getSender(), this.getViewer(), this.group, this.messageWrapper.getPlaceholders()).build());

        if (components != null) componentBuilder.append(components);

        return TextComponent.toPlainText(componentBuilder.create());
    }
}
